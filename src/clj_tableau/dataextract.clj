(ns clj-tableau.dataextract)

(defn extract
  "Open TDE file"
  [filename]
  (com.tableausoftware.DataExtract.Extract. filename))

(defmacro with-extract
  "Open an extract and ensures that it will closed
  when leaving the closure. Binds the extract handle
  when binding is [var filename]"
  [bindings & body]
  (let [form (bindings 0) file (bindings 1)]
    `(with-open [~form (com.tableausoftware.DataExtract.Extract. ~file)]
       ~@body
       )
    )
  )

(defn table
  "Open or create a table inside a TDE file"
  [name definition]
  ()
  [name]
  (table name nil)
  )