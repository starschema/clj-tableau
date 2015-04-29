(ns clj-tableau.dataextract
  (:import (com.tableausoftware.DataExtract Extract Type TableDefinition Collation)))

(defn extract
  "Open TDE file"
  [filename]
  {:pre [(some? filename)]}
  (Extract. filename))

(defmacro with-extract
  "Open an extract and ensures that it will closed
  when leaving the closure. Binds the extract handle
  when binding is [var filename]"
  [bindings & body]
  (let [form (bindings 0) file (bindings 1)]
    `(with-open [~form (Extract. ~file)]
       ~@body
       )))

(defn- get-table-definition
  "Create TableDefinition from table definition map"
  [{:keys [collation columns] :or {collation Collation/BINARY}}]
  {:pre [(vector? columns)]}
  (let [defobj (TableDefinition.)]
    (.setDefaultCollation defobj collation)
    (dorun
      (map
        (fn [col-entry]
          (if (= Type (type (second col-entry)))
            (.addColumn defobj (first col-entry) (second col-entry))
            (.addColumnWithCollation
              defobj
              (first col-entry)
              (nth (second col-entry) 0)
              (nth (second col-entry) 1))
            ))
        (partition 2 columns)))
    defobj
    ))

(defn- parse-table-definition
  [defobj]
  '()
  )

(defn table
  "Open or create a table inside a TDE file. Extract argument must be a valid
  extract handle. If definition provided and table does not exist it will be
  created otherwise opened."
  ([^Extract extract tablename definition]
   {:pre [(some? tablename) (some? extract)]}
   (let [tableobj
         (if (.hasTable extract tablename)
           (.openTable extract tablename)
           (.addTable extract tablename (get-table-definition definition)))]
     {:obj    tableobj
      :def    (or definition (parse-table-definition (.getTableDefinition tableobj)))}))

  ([extract tablename]
   (table extract tablename nil)))