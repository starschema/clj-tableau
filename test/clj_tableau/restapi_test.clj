(ns clj-tableau.restapi-test
  (:require [clojure.test :refer :all]
            [clj-tableau.restapi :refer :all]
            [clojure.tools.logging :as log]))

(deftest logon-logoff
  (testing "if we can login and logoff from the tableau server"
    (let [session (logon-to-server "http://tableau-server.domain.com" "" "admin" "admin")]
      (is session)
      (is (get session :token))
      (is (get session :host))
      (is (get session :siteid))
      (is (get session :content-url))
      (signout session))))

(deftest create-update-delete-user
  (testing "the user create/delete functions"
    (with-tableau-rest-api [session ["http://tableau-server.domain.com" "" "admin" "admin"]]
      (let [userid (add-user session (str "testuser" (rand-int 10000000)))]
        (is userid)
        ; TODO check if user created
        (update-user session userid "Test User1" "test@domain.com")
        ; TODO check if user deleted
        (delete-user-from-site session userid)))))

(deftest add-remove-user-from-group
  (testing "the add / remove user from group functions"
    (with-tableau-rest-api [session ["http://tableau-server.domain.com" "" "admin" "admin"]]
      (let [groupid (get-group-id session "LDAP Sync Test")]
        (is (= (type groupid) String))
        (is (get-users-from-group session groupid))
        (let [userid (add-user session (str "testuser" (rand-int 10000000)))]
          (is userid)
          (log/debug "userid" userid)
          (add-user-to-tableau-group session groupid userid)
          (is (get-users-from-group session groupid))
          (remove-user-from-tableau-group session groupid userid)
          (delete-user-from-site session userid))))))


(deftest get-list-of-users
  (testing "getting list of users"
    (let [session (logon-to-server "http://tableau-server.domain.com" "" "admin" "admin")]
      (is session)
      (let [users (get-users-on-site session)]
        (log/debug "users: " users)))))
