# clj-tableau

A Clojure library designed to interact with [Tableau](http://tableau.com) APIs including [Data Extract API](http://www.tableausoftware.com/data-extract-api), [REST Server API](http://www.tableau.com/learn/tutorials/on-demand/rest-api) and the [undocumented Web Service API](http://community.tableau.com/groups/dev-community/blog/2013/04/24/using-the-undocumented-rest-api-authentication-and-invocation-of-tableau-server).

[![Build Status](https://travis-ci.org/starschema/clj-tableau.svg?branch=master)](https://travis-ci.org/starschema/clj-tableau)

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

REST API support is a new addition to clj-tableau. Our implementation uses the `clj-http` and `data.xml` clojure libraries. This new namespace allows to query site, group & user related information supplied by Tableau Server.  

The first step is to require the namespace in our REPL:

    (require '[clj-tableau.restapi :refer :all])
    
You have two options for logging onto your desired Tableau Server:

##### Using logon-to-server:

    (logon-to-server "http://tableau-server.com" "TestSite" "JohnDoe" "secret")
    
##### Using with-tableau-rest-api

    (with-tableau-rest-api [sess ["http://tableau-server.com" "TestSite" "JohnDoe" "secret"]]
                             (->>
                               (get-users-on-site sess)))

Most of the functions require you to pass an existing session. For example if you would like to know the list of groups on the server then all you need to do is:

    (get-groups-on-site (logon-to-server "http://tableau-server.com" "TestSite" "JohnDoe" "secret"))
    
The same goes for listing all users on the site:

    (get-users-on-site (logon-to-server "http://tableau-server.com" "TestSite" "JohnDoe" "secret"))

For a full list of available functions please refer to:

* logon-to-server
* signout
* with-tableau-rest-api
* update-user
* add-user
* delete-user-from-site
* get-users-on-site
* get-users-from-group
* get-groups-on-site
* get-group-id
* add-user-to-tableau-group
* remove-user-from-tableau-group
* query-user-on-site

### Examples

Example codes are located under `/doc` folder. 
* For tableau data extracts you can check the clojure version of the [make order](https://github.com/starschema/clj-tableau/blob/master/doc/examples/make-order.clj) tableau sample. 
* For Rest API please check this a [sample group query example.](https://github.com/starschema/clj-tableau/blob/master/doc/examples/tableau-group-inspect.clj)

## License

Copyright © 2015 Tamas Foldi ([@tfoldi](http://twitter.com/tfoldi)), [Starschema](http://www.starschema.net/) Ltd.

Distributed under BSD License.
