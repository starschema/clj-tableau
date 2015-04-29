(defproject clj-tableau "0.1.0-SNAPSHOT"
            :description "Clojure bindings for Tableau API"
            :url "http://github.com/starschema/clj-tableau"
            :license {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}
            :resource-paths ["resources/jna.jar" "resources/dataextract.jar"]
            :dependencies [[org.clojure/clojure "1.6.0"]
                           [org.clojure/tools.nrepl "0.2.10"]])
