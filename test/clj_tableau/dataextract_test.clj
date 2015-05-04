(ns clj-tableau.dataextract-test
  (:require [clojure.test :refer :all]
            [clj-tableau.dataextract :refer :all])
  (:import (com.tableausoftware.DataExtract Type Collation)))

(def table2-definition
  {
   :collation Collation/EN_GB
   :columns   ["Purchased" Type/DATETIME
               "Product" Type/CHAR_STRING
               "uProduct" Type/UNICODE_STRING
               "Price" Type/DOUBLE
               "Quantity" Type/INTEGER
               "Taxed" Type/BOOLEAN
               "Expiration Date" Type/DATE
               "Produkt" [Type/CHAR_STRING Collation/DE]]})

(def test2-data
  (repeatedly
    #(vec [ [2012 7 3 11 40 12 4550] "Beans" "uniBeans" 1.08
           (rand-int 100) (rand-nth [true false]) [2029 1 1] "Bohnen" ] )))

(deftest extract-creation
  (testing "If the tde file is created."
    (with-extract [t "test1.tde"]
                  (is (.exists (clojure.java.io/as-file "test1.tde")) )
                  )
    (clojure.java.io/delete-file "test1.tde")
    (is (not (.exists (clojure.java.io/as-file "test1.tde")))))
  (testing "create basic extract with add-row and add-rows")
    (with-extract [test2 "test2.tde"]
                  (let [tab (table test2 "Extract" table2-definition)]
                    (add-rows tab (take 10 test2-data) )
                    (add-row tab (first (take 1 test2-data)))
                    ))
  (clojure.java.io/delete-file "test2.tde"))

