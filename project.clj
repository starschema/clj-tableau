(defproject clj-tableau "1.0.0-SNAPSHOT"
            :description "Clojure bindings for Tableau APIs (Extract & Rest)"
            :url "http://github.com/starschema/clj-tableau"
            :license {:name "Eclipse Public License"
                      :url  "http://opensource.org/licenses/BSD-2-Clause"}
            :resource-paths ["resources/jna.jar" "resources/dataextract.jar"]
            :dependencies [[org.clojure/clojure "1.6.0"]
                           [org.clojure/tools.nrepl "0.2.10"]])
