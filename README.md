# clj-tableau

A Clojure library designed to interact with [Tableau](http://tableau.com) APIs including [Data Extract API](http://www.tableausoftware.com/data-extract-api), [REST Server API](http://www.tableau.com/learn/tutorials/on-demand/rest-api) and the [undocumented Web Service API](http://community.tableau.com/groups/dev-community/blog/2013/04/24/using-the-undocumented-rest-api-authentication-and-invocation-of-tableau-server).

## Installation

`clj-tableau` is available as a Maven artifact from [Clojars](https://clojars.org/clj-tableau):

[![Clojars Project](http://clojars.org/clj-tableau/latest-version.svg)](http://clojars.org/clj-tableau)

`clj-tableau` supports clojure 1.6.0 and higher.

To use tableau extract API you should [download](http://www.tableausoftware.com/data-extract-api) and manually put the dependent jar files (`dataextract.jar` and `jna.jar`) to the `/resources` folder. Please make sure that the TDE library binaries are included in the PATH environment variable.

## Usage

### Data Extract API

The TDE related functionality is provided by the `clj-tableau.dataextract` namespace.

First, require it in the REPL:

    (require '[clj-tableau.dataextract :refer :all])

Or in your application:

    (ns my-app.core
      (:require [clj-tableau.dataextract :refer :all]))

Opening or creating a TDE file:

    (with-extract [tdefile "myfile.tde"]
      <yourcode>
    )
`tdefile` handle will be closed when you leave the closure. To create a new table inside `tdefile` and add two rows in it:

    ; define table structure
    (def mytable-def
      ; collation can be omitted
      ;
      { :collation Collation/EN_GB
        :columns [ :foo Type/String
                   :bar Type/Integer]})

    (with-extract [tdefile "myfile.tde"]
      (->
        ; open or create new table in tde file
        ; if tde exists, mytable-def parameter
        ; is not required
        (table tdefile "Extract" mytable-def)
        ; add-rows take table + sequence of vectors
        ; with column values
        (add-rows '(["first" 1] ["second" 2]))))

You can add lines one-by-one with `add-row` or pass (lazy) sequences to `add-rows` function. Row should be  `vec` with same number of elements as defined columns in extract table.

If you append rows to an existing TDE file then the table definition parameter of `table` function can be omitted.

### Rest & Web Servie API

Rest and Web Service API namespaces are not included in the current release as those need some cleanup. The implementation uses `clj-http` and `data.xml` and will be released soon.

### Examples

Example codes are located under `/doc` folder. For tableau data extracts you can check the clojure version of the [make order](https://github.com/starschema/clj-tableau/blob/master/doc/examples/make-order.clj) tableau sample.

## License

Copyright © 2015 Tamas Foldi ([@tfoldi](http://twitter.com/tfoldi)), [Starschema](http://www.starschema.net/) Ltd.

Distributed under BSD License.