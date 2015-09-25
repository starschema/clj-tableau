(ns clj-tableau.repository
  (:require [korma.db]
            [korma.core :refer :all]
            [clojure.java.jdbc :as jdbc]
            [clojure.edn :as edn]
            )
  )


(def TABLEU-DB-CONFIG
  (edn/read-string (slurp "resources/tableau-db-spec.edn")))

(defn initialize-repository-connection []
  ""
  (let [dbspec (TABLEU-DB-CONFIG :dbspec)]
    (korma.db/defdb db (korma.db/postgres {:db       (dbspec :db)
                                           :user     (dbspec :user)
                                           :password (dbspec :password)
                                           :host     (dbspec :host)
                                           :port     (dbspec :port)
                                           })))
  )


(defentity sites)
(defentity users)
(defentity system_users)


(defn user-luid-of [username]
  "Returns the luid of a provided user"
  (select users
          (fields :system_users.name :luid)
          (from system_users)
          (where (= :users.id :system_users.id))
          (where (= :system_users.name username))
          )
  )

(defn site-luid-of [site-name]
  "Find ID of provided site name"
  (select sites
          (fields :luid)
          (where (= :name site-name)))
  )








