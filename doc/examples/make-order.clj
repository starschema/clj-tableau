(require [clj-tableau.dataextract :refer :all])

(def table-definition
  {
   :collation Collation/EN_GB
   :columns   ["Purchased"        Type/DATETIME
               "Product"          Type/CHAR_STRING
               "uProduct"         Type/UNICODE_STRING
               "Price"            Type/DOUBLE
               "Quantity"         Type/INTEGER
               "Taxed"            Type/BOOLEAN
               "Expiration Date"  Type/DATE
               "Produkt"         [Type/CHAR_STRING Collation/DE]]})

(def test-data
  (repeatedly
    #(vec [ [2012 7 3 11 40 12 4550] "Beans" "uniBeans" 1.08
           (rand-int 100) (rand-nth [true false]) [2029 1 1] "Bohnen" ] )))

(defn print-table-definition
  [table]
  (dorun (map println (get table :def)))
  table)

(try
  (with-extract [order "order-clojure.tde"]
                (->
                  (table order "Extract" table-definition)
                  (print-table-definition)
                  (add-rows (take 10 test-data))))
  (catch Exception e
    (clojure.stacktrace/root-cause e)))