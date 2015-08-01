(ns tableau-group-inspect.core
  (:require [clj-tableau.restapi :refer :all]))

(defn filter-searched-users
  [searched-users users]
  (filter #(some #{(val %)} searched-users) users))


(defn search-for-users-in-group
  [sess users-to-search group]
  (->>
    (get-users-from-group sess (key group))
    (filter-searched-users users-to-search)
    (vals)))


(defn build-map-with-user-groups
  [users users-in-groups]
  (into {}
        (map (fn [user]
               {user
                (doall
                  (map second (filter #(contains? (set (first %)) user) users-in-groups)))}
               ) users)))

(defn get-filtered-users-in-groups
  [sess users groups]
  (pmap
    #(vector (search-for-users-in-group sess users %) (val %))
    groups))

(defn -main
  [& args]
(with-tableau-rest-api [tableau_session ["http://tableau-server.com" "TestSite" "JohnDoe" "secret"]]
                         (let [users ["123456789" "234567890"]
                               groups (get-groups-on-site tableau_session)]
                             (->>
                               (get-filtered-users-in-groups tableau_session users groups)
                               (build-map-with-user-groups users)
                               (doall)
                               (time)))))
