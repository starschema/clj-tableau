(ns clj-tableau.dataextract
  (:import (com.tableausoftware.DataExtract Extract Type TableDefinition Row Collation)))

(defn extract
  "Open TDE file"
  [filename]
  {:pre [(some? filename)]}
  (Extract. filename))

(defmacro with-extract
  "Open an extract and ensures that it will closed
  when leaving the closure. Binding form is [var filename]"
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
            ; Column_name Type
            (.addColumn defobj (name (first col-entry)) (second col-entry))
            ; Column_name [Type Collation]
            (.addColumnWithCollation
              defobj
              (name (first col-entry))
              (first (second col-entry))    ; Type
              (second (second col-entry)))  ; Collation
            ))
        (partition 2 columns)))
    defobj
    ))

(defn- parse-table-definition
  "Generate standard map from TableDefinition object's column attributes"
  [^TableDefinition defobj]
  (mapv
    (fn [idx]
      {:index idx
       :name (.getColumnName defobj idx)
       :type (.getColumnType defobj idx)
       :collation (.getColumnCollation defobj idx)
       })
    (range (.getColumnCount defobj))))

(defn add-row
  [table row]
  (let [rowobj (Row. (get table :defobj) )]
    (dorun
      (map
        (fn [val def]
          (case (.toString (get def :type))
            "BOOLEAN" (.setBoolean rowobj (get def :index) val)
            "CHAR_STRING" (.setCharString rowobj (get def :index) val)
            "DATE" (apply #(.setDate rowobj %1 %2 %3 %4) (get def :index) val)
            "DATETIME" (apply #(.setDateTime rowobj %1 %2 %3 %4 %5 %6 %7 %8) (get def :index) val)
            "DOUBLE" (.setDouble rowobj (get def :index) val)
            "DURATION" (apply #(.setDuration rowobj %1 %2 %3 %4 %5 %6) (get def :index) val)
            "INTEGER" (.setInteger rowobj (get def :index) val)
            "UNICODE_STRING" (.setString rowobj (get def :index) val)))
        row (get table :def)))
    (.insert (get table :tableobj) rowobj)))

(defn add-rows
  "Adds multiple rows to an extract table. Rows should be sequence"
  [table rows]
  (dorun (map (fn [row] (add-row table row)) rows)))

(defn table
  "Open or create a table inside a TDE file. Extract argument must be a valid
  extract object handle. If definition provided and table does not exist it will be
  created otherwise opened."
  ([^Extract extract tablename definition]
   {:pre [(some? tablename) (some? extract)]}
   (let [tableobj
         (if (.hasTable extract tablename)
           (.openTable extract tablename)
           (.addTable extract tablename (get-table-definition definition)))]
     {:tableobj    tableobj
      :defobj      (.getTableDefinition tableobj)
      :def         (parse-table-definition (.getTableDefinition tableobj))}))

  ([extract tablename]
   (table extract tablename nil)))