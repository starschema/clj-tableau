(ns clj-tableau.restapi
  (:require [clj-http.client :as client]
            [clojure.zip :refer [xml-zip]]
            [clojure.data.zip.xml :refer [xml-> xml1-> attr text]]
            [clojure.data.xml :as xml]
            [clojure.tools.logging :as log])
  (:import (clojure.lang ExceptionInfo)))

(def ^:dynamic page-size 1000)

(defn- tableau-url-for
  "Constructs server API url. Target can be hostname or session returned from logon-to-server"
  [target api-path]
  (if (= (type target) java.lang.String)
    ; connect to host
    (str target "/api/2.0" api-path)
    ; connect to already established session (target )
    (str (get target :host) "/api/2.0"
         (if (= api-path "/auth/signout")
           api-path
           (str "/sites/" (get target :siteid) "/" api-path)))))

(defn- get-zip
  "Get xml-zip from http.client response"
  [http-response]
  (if (get http-response :body)
    (-> (get http-response :body)
        xml/parse-str
        xml-zip)))


(defn- get-status-from-http-exception
  "Get HTTP exception code from a clojure exception info throwed by clj-http"
  [e]
  (get (ex-data e) :status))

(defn- http
  "Perform a http call to tableau server. Host can be hostname or session"
  [method host api-path http-params]
  (get-zip
    ((resolve (symbol (str "clj-http.client/" method)))
      (tableau-url-for host api-path)
      (merge http-params {:headers {"X-Tableau-Auth" (get host :token)}}))))

(defn- logindata
  "Creates XML request for logon call"
  [site name password]
  (xml/emit-str
    (xml/element :tsRequest {}
                 (xml/element :credentials {:name     name
                                            :password password}
                              (xml/element :site {:contentUrl site})))))

(defn- get-users-from-ts-response
  [ts-response]
  (xml-> ts-response :users :user (juxt (attr :name) (attr :id))))

(defn- get-users-from-tableau-response
  [ts-response]
  (xml-> ts-response :users :user (juxt (attr :id) (attr :name))))

(defn- updateuserdata
  "Creates XML request for user update method"
  [fullname email]
  (xml/emit-str
    (xml/element :tsRequest {}
                 (xml/element :user {
                                     :fullName fullname
                                     :email    email
                                     }))))

(defn- adduserdata
  "Creates XML request for user creation"
  [name]
  (xml/emit-str
    (xml/element :tsRequest {}
                 (xml/element :user {:name     name
                                     :siteRole "Interactor"
                                     }))))

(defn- add-or-remove-user-from-groupdata
  "Creates XML request for add & remove user methods"
  [userid]
  (xml/emit-str
    (xml/element :tsRequest {}
                 (xml/element :user {
                                     :id userid
                                     }))))

(defn delete-user-from-site
  "Removes user from site entirely
  DELETE /api/api-version/sites/site-id/users/user-id"
  [session userid]
  (http "delete" session (str "/users/" userid) {}))

(defn update-user
  "Update users email and fullname "
  [session userid fullname email]
  (http "put" session (str "/users/" userid) {:multipart [{:name    "title"
                                                           :content (updateuserdata fullname email)}]}))


(defn add-user
  "Adds user to the site. If user already exist, do nothing, otherwise raise exception"
  [session name]
  (try
    (let [ts-response (http "post" session "/users/"
                            {:multipart [{:name    "title"
                                          :content (adduserdata name)}]})]
      (log/debug ts-response)
      (xml1->
        ts-response
        :user (attr :id)))
    (catch ExceptionInfo e
      (log/debug "Exc: " e)
      (if (= (get-status-from-http-exception e) 409)
        (log/debug "User " name " already added to this site, ignoring request.")
        (throw e)))))


(defn logon-to-server
  "Logon to tableau server by invoking /auth/signin, returns map with token,
  site id and hostname"
  [host site name password]
  (let [ts-response (http "post" host "/auth/signin"
                          {:multipart [{:name    "title"
                                        :content (logindata site name password)}]})]
    (log/debug ts-response)
    {:token       (xml1-> ts-response
                          :credentials (attr :token)
                          )
     :content-url (xml1-> ts-response
                          :credentials
                          :site (attr :contentUrl)
                          )
     :siteid      (xml1-> ts-response
                          :credentials
                          :site (attr :id))
     :host        host}))


(defn get-users-on-site
  "Iterates on site users defined by session. Page size is 100"
  [session]
  (loop [page-number 1 allusers '()]
    (let [ts-response (http "get" session "/users/"
                            {:query-params {:pageSize   page-size
                                            :pageNumber page-number}})
          users (get-users-from-ts-response ts-response)]

      ;      (log/debug (str "On page " page-number " -> " users))
      (if (xml1-> ts-response
                  :pagination)
        (if (> (* page-size page-number) (read-string (xml1-> ts-response
                                                              :pagination
                                                              (attr :totalAvailable))))
          (do
            (log/debug "All users downloaded")
            (->>
              (conj allusers users)
              (flatten)
              (apply hash-map)))
          (recur (inc page-number) (conj allusers users)))))))

(defn signout
  "Logoff from server"
  [session]
  (http "post" session "/auth/signout" {}))

(defn get-group-id
  "Return the group-id of a specific group
  GET /api/api-version/sites/site-id/groups"
  [session group]
  (let [response
        (http "get" session "groups" {:query-params {:pageSize 1000}})]
    (->>
      (xml-> response
             :groups
             :group
             (juxt (attr :id) (attr :name)))
      (vec)
      (partition 2)
      (filter #(= group (second %)))
      (first)
      (first))))

(defn get-users-from-group
  "Gets users from Tableau group using the Tableau 9 REST API
  GET /api/api-version/sites/site-id/groups/group-id/users"
  [session group-id]
  (loop [page-number 1 group-members '()]
    (let [response
          (http "get" session (str "groups/" group-id "/users")
                {:query-params {:pageSize   page-size
                                :pageNumber page-number}})
          users (xml-> response
                       :users
                       :user
                       (juxt (attr :id) (attr :name)))]

      (if (> (* page-size page-number) (read-string (xml1-> response
                                                            :pagination
                                                            (attr :totalAvailable))))
        (do
          (log/debug "All users downloaded from tableau group " group-id)
          (->>
            (conj group-members users)
            (flatten)
            (apply hash-map)))
        (recur (inc page-number) (conj group-members users))))))

(defn add-user-to-tableau-group
  "Add users to Tableau group
  POST /api/api-version/sites/site-id/groups/group-id/users/"
  [session group-id user-id]
  (log/debug (str "Adding user " user-id " to group " group-id))
  (try
    (http "post" session (str "groups/" group-id "/users")
          {:multipart [{:name    "title"
                        :content (add-or-remove-user-from-groupdata user-id)}]})
    (catch ExceptionInfo e
      (log/debug "Exc: " e)
      (if (= (get-status-from-http-exception e) 409)
        (log/debug "User " name " already added to this group, ignoring request.")
        (throw e)))))

(defn remove-user-from-tableau-group
  "Removes user from Tableau group
  DELETE /api/api-version/sites/site-id/groups/group-id/users/user-id"
  [session group-id user-id]
  (try
    (if (and group-id user-id)
      (http "delete" session (str "groups/" group-id "/users/" user-id) {}))
    (catch ExceptionInfo e
      (log/error (type e) ": Error when removing user " user-id " " (ex-data e))
      (throw e))))

