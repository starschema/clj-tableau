(defproject clj-tableau "1.0.6-SNAPSHOT"
            :description "Clojure bindings for Tableau APIs (Extract & Rest)"
            :url "http://github.com/starschema/clj-tableau"
            :license {:name "Eclipse Public License"
                      :url  "http://opensource.org/licenses/BSD-2-Clause"}
            :resource-paths ["resources/jna.jar" "resources/dataextract.jar"]
            :dependencies [[org.clojure/clojure "1.6.0"]
                           [clj-http "1.1.0"]
                           [org.clojure/data.xml "0.0.8"]
                           [org.clojure/data.zip "0.1.1"]
                           [org.clojure/tools.logging "0.3.1"]
                           [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                              javax.jms/jms
                                                              com.sun.jdmk/jmxtools
                                                              com.sun.jmx/jmxri]]
                           [org.clojure/tools.nrepl "0.2.10"]
                           [korma "0.4.2"]
                           [org.clojure/java.jdbc "0.3.7"]
                           [postgresql "9.3-1102.jdbc41"]
                           ])
