(require [clj-tableau/dataextract])

(def test-data
  []
  )

(def table-definition
  {
   :collation Collation/EN_GB
   :columns   [
               "Purchased"        Type/DATETIME
               "Product"          Type/CHAR_STRING
               "uProduct"         Type/UNICODE_STRING
               "Price"            Type/DOUBLE
               "Quantity"         Type/INTEGER
               "Taxed"            Type/BOOLEAN
               "Expiration Date"  Type/DATE
               "Produkt"         [Type/CHAR_STRING Collation/DE]
               ]})

(with-extract [foo "foobar.tde"]
              (table foo "Extract" table-definition)
              )