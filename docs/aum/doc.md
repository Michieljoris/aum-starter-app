---
title: Aum

language_tabs: # must be one of https://git.io/vQNgJ
  - clojure

toc_footers:
  - <a href='https://github.com/lord/slate'>Documentation Powered by Slate</a>

includes:
  - errors

search: true
---


# Table of Contents

1.  [Getting started](#orgd48ea8f)
    1.  [Useful docs to read first](#orgfe9568d)
        1.  [React](#org5395129)
        2.  [Om-next](#orgc6a6b02)
        3.  [Fulcro handbook](#org4fbe2d2)
        4.  [See om-next docs for more useful links](#org8350914)
    2.  [Install](#org12379d2)
    3.  [Starter app](#orgdfef98f)
        1.  [Install](#org8ef2997)
        2.  [Run in development mode](#org9a9348a)
        3.  [Run in production mode](#org73e0430)
2.  [Core concepts](#org4fe6e9e)
    1.  [It&rsquo;s om-next.](#org16e2935)
3.  [Build system](#orgd5788de)
4.  [Environment](#orgf09d11d)
5.  [Config](#orgcca32f7)
6.  [App starting process](#org0fa9fbc)
    1.  [dev](#org480cb3f)
        1.  [Backend](#orgecd29c9)
        2.  [Frontend](#org604d6a2)
    2.  [prod](#org538b212)
7.  [Generic save records](#orged0fbfd)
8.  [Have backend return calculated data](#orge08dc52)
    1.  [Calculate something over a (sub)query](#orgd61e8e5)
    2.  [Define a read key in the backend](#orgf882745)
    3.  [Redirect a read to a custom-read](#org82d87f8)
9.  [Use datomic pull syntax to query mysql database](#orgd47bd3d)
10. [Modules](#org401697e)
    1.  [icons](#orgc8c8b97)
    2.  [Security](#orge323848)
    3.  [Internationalization](#org90cdb43)
11. [Websockets](#org54ec75b)
12. [Database](#org3aa050a)
    1.  [Write validation](#org232e8e1)
    2.  [Sql validation](#orgcdfb569)
    3.  [Sql process-params, process-result](#org3a7c9be)
13. [Frontend](#orgeded58f)
    1.  [make-cmp and om-data](#org6a1cbfa)
    2.  [Use pages to organize your ui](#org9383bcc)
    3.  [Client only keys](#org2e18643)
    4.  [Validation of form values](#orgc37ab01)
    5.  [Syncing of front and backend](#org0cd26cf)
    6.  [Generic recursive read with hooks](#org3003cf1)
        1.  [Intro](#orgca2628e)
        2.  [Adding hooks for keys and joins in the root query for returning values and building remote query](#org1420a88)
    7.  [Garbage collection](#org08c33e1)
    8.  [Internationalization](#orgfe70b0c)
    9.  [Post remote](#org5efd421)
    10. [Pre-merge hooks](#org5d93ee7)
    11. [Merging pushed data](#orgf1b5913)
    12. [Generic undo/redo/revert.](#org4c022d0)
    13. [Run backend in frontend (for testing for example)](#org926eba5)
    14. [Test runner](#orga6e5d53)
    15. [Snapshot testing](#orgca42ee9)
    16. [Whole stack testing](#org90464b7)
    17. [Inspector](#org6be0b67)
    18. [Dev-cards](#org279a68f)
14. [Testing](#org1035e35)
15. [Debug production/staging](#orgc812736)
16. [Misc](#orga67d060)
    1.  [Querying other sources than a mysql database](#org19e36c9)
        1.  [Using more than one remote in the frontend](#orgee528c3)
        2.  [Returning data fetched from another source asynchronously](#org416f63d)
17. [config](#orgacb9f79)
18. [(sql) validation](#org6f99536)
19. [sql process-params, process-result](#org57689f1)
20. [Read permissions and create/update/delete permissions, and validations of om-queries](#org15f5483)
21. [frontend testing](#orge92076a)
22. [Deciding on selected group](#orga1f2b4e)
23. [Trying queries](#org3f0f56a)
    1.  [try-om-query](#orgf8d103a)
    2.  [Try sql query](#org85cd89d)
    3.  [Try/test frontend parser.](#org0900db2)
24. [Start aum with different ports and db:](#org7d81c71)
25. [pathopt](#org1969239)
26. [Adding hooks for keys and joins in the root query for returning values and building remote query](#org66117c9)
    1.  [Principles](#orga1e721b)
    2.  [Examples](#org3fdd081)
        1.  [VALUE example](#org4431aad)
        2.  [REMOTE example](#org736fa5d)
    3.  [Notes](#orgf91311e)
27. [Have backend return calculated data](#orgfc1a6f7)
    1.  [Calculate something over a (sub)query](#orgc2accf8)
    2.  [Define a read key in the backend](#org68a2fa1)
    3.  [Redirect a read to a custom-read](#org4fb8e73)
28. [invalidation](#org4727581)



<a id="orgd48ea8f"></a>

# Getting started


<a id="orgfe9568d"></a>

## Useful docs to read first


<a id="org5395129"></a>

### React

<https://reactjs.org/docs/getting-started.html>

Om-next is built on react. Om-next components are basically classical react
components with a layer over them so om-next can do it&rsquo;s own optimisations and
control the render cycle. But just like in react you have access to the various
lifecycle hooks (but leave shouldComponentUpdate alone, this is managed by
om-next). You pass in props to subcomponents and components can have their own
local state. Basically om-next generates a tree of data from the app-state by
applying the root query of the app over it every time a transact is done or a
merge of data with app-state (in the reconciler for instance when data is
returned and the callback invoked with it). This data is then supplied to the
root component and the new ui state calculated and rendered. Which is then
static till the next transaction or merge. This sounds rather inefficient, but
the way React is designed it&rsquo;s not in practice. Also om-next itself won&rsquo;t even
ask React to re-render a component if it decides that the props that are passed
to it haven&rsquo;t changed from the last time it was rendered.


<a id="orgc6a6b02"></a>

### Om-next

[https://github.com/omcljs/om/wiki/Documentation-(om.next)](https://github.com/omcljs/om/wiki/Documentation-(om.next))


<a id="org4fbe2d2"></a>

### Fulcro handbook

<http://book.fulcrologic.com/>
Most of it is about how om-next itself works. The solutions for a more practical
om-next are a bit different, and in some ways diverge somewhat from the &rsquo;om-next
way&rsquo; of doing things.


<a id="org8350914"></a>

### See om-next docs for more useful links

Such as for graphql, falcor, datomic etc.

&ldquo;Om-next by leveraging lisp and immutable values combines the best ideas of
graphql and falcor in a less cumbersome and more flexible manner.&rdquo;


<a id="org12379d2"></a>

## Install

To pull in all the tools and libs to build an aum app add

`````clojure
    aum {:git/url "https://github.com/michieljoris/aum.git",
         :sha "577daf362c3f81e08d43f654ef0bbf3ddc93e015"
         :tag "master"},
````

to your dependencies.

To actually build an app it&rsquo;s a good idea to start with a minimal setup, see the
following section.


<a id="orgdfef98f"></a>

## Starter app

TODO-doc: add github link to aum-starter-app

There is a repo that&rsquo;s an app using aum libs, but has minimal content. This can
be used to try out building features/pages.

Clone it and follow the following instructions.


<a id="org8ef2997"></a>

### Install

Prerequisites:

-   mysql
-   node
-   nvm
-   java 1.7 or greater

[Install clojure](https://clojure.org/guides/getting_started)

In the starter repo do:

nvm install
nvm use

to make sure the proper version of node is used.

And then:

bin/dev-install

For development also do this:

Make sure pagora.aum, pagora.clj-utils and pagora.revolt repos are in the same parent dir as this repo.

So if this repo is in ~/src/aum-starter-app for example, then you should
also have for instance ~/src/aum  checked out to the most recent master.

TODO-aum: get this working properly!!!
    This is so that tools.deps can resolve the dependencies locally. If you
    want deps from github replace local-deps with git-deps in the bin/dev-backend script


<a id="org9a9348a"></a>

### Run in development mode

It&rsquo;s practical to run backend and frontend separately. It&rsquo;s rare you need
restart both, but being able to restarting one or the other sometimes is useful.
Also sometimes you only need or want to work on the backend.

After bin/dev-install:

bin/dev-backend

which will get you a clj repl in the terminal and an nrepl server.

To set config settings, for example the db, do:

DB<sub>NAME</sub>=chinchilla bin/dev-backend

See config.clj for possible settings and their defaults (replace
hyphens with underscores and upcase the config key).

Connect your editor&rsquo;s repl to the nrepl server at port 5700.

In dev mode src-dev/clj/user.clj gets loaded (see resources/revolt.edn under the
revolt.plugin/rebel key). In deps.edn src-dev should be included in a paths
vector.

The sexpr (restart) is uncommented in that ns so it will be executed which will
(re)start the backend app.

To compile the frontend, in another terminal do:

bin/dev-frontend

which will get you a cljs repl in your terminal.

See app at localhost:8080/app

TODO-doc: test and document cljs repl

Alternatively start figwheel repl in emacs when running cider:
cider-jack-in-cljs (SPACE-m-&ldquo; in Doom emacs).

Entry point of backend in development is at clj.user ns. It has the lifecycle methods.

Entry point of frontend is at app.frontend.core. The aum/init fn initializes a
websocket and starts it. The react component tree gets mounted after websocket first
open event is received.

NOTE: after building a prod jar, restart both backend and figwheel processes.
This is because the out dir is cleaned before building and the dev versions of both css and js
need to be built again. Or try modify a scss file and a cljs file to
kickstart recompile.


<a id="org73e0430"></a>

### Run in production mode

TODO-aum: the build task doesn&rsquo;t include deps in aliases into the uberjar. Need to
fix the build task, so to create an uberjar uncomment the pagora.aum, pagora.revolt and
pagora.clj-utils deps in deps.edn.

Build production jar at dist/app.jar with:

bin/prod-build

This&rsquo;ll fetch node modules and build the js bundle as well.

Set db user/password/url/db-name, server port and ip, and what logs you would
like by setting the various environment variables and run jar (in dist dir):

TODO-aum: test starter app prod jar!!!
    CLJ<sub>ENV</sub>=prod DB<sub>USER</sub>=test DB<sub>PASSWORD</sub>=abc DB<sub>URL</sub>=&ldquo;*/localhost:3306*&rdquo; DB<sub>NAME</sub>=chinchilla<sub>development</sub> QUERY<sub>LOG</sub>=true SQL<sub>LOG</sub>=true HTTP<sub>LOG</sub>=false SERVER<sub>PORT</sub>=8081 SERVER<sub>IP</sub>=0.0.0.0 NEW<sub>RELIC</sub><sub>LICENSE</sub><sub>KEY</sub>=&ldquo;<some newrelic key>&rdquo; java -javaagent:./newrelic-agent.jar -Dnewrelic.config.file=./newrelic.yml   -Dnewrelic.environment=production -jar dist/aum.jarlk

or just

bin/prod-run

Or all in one cmd:

bin/prod-build-and-run

If tools.deps complains about dirs already existing when cloning repos delete ~/.gitlibs

Clearing classpath cache in ~/.clojure might also help.

See app at <http://localhost:8090>

Entry point of backend in production is at app.core (the -main fn).


<a id="org4fe6e9e"></a>

# Core concepts


<a id="org16e2935"></a>

## It&rsquo;s om-next.

The idea is to stay as close as possible to the original idea of om-next as just
a thin layer on the top of react, but extend some concepts so at the very least
a straightforward crud app can be built really simply and quickly, with simple
tools to facilitate both front and backends development.

Om-next itself is isomorphic, meaning it can ran on either front or backend. Aum
extends om-next, and some of it can be used on both front and backend, but in
general it focuses more on making om-next useful in a practical way. So in the
backend the om-next parser is implemented to query a mysql database, with
security and validation mechanisms.

On the frontend aum implements a parser that in most cases will do the
right thing in denormalizing queries over the app state. And there are mechanism
for dealing with multiple remotes, websockets, error handling, correcting
optimistic updates etc.

Aum has its own thin layers over the reconciler and parser but still uses defui,
om/transact! etc


<a id="orgd5788de"></a>

# Build system

TODO-doc: revolt
webpack and foreign-libs
bin scripts


<a id="orgf09d11d"></a>

# Environment

You can require pagora.aum.environment in both front and backend. The current evironment
is in the pagora.aum.environment/environment var or call functions like is-development? from that
namespace. The default environment is :dev. Start the app with
CLJ<sub>ENV</sub>=[production|staging|testing] to change the environment.


<a id="orgcca32f7"></a>

# Config

Config is defined in multimethods like this:

```clojure
    (ns app.config)
    
    (defmethod aum/config :common [_]
      {:timbre-log-level :error
       :app-path "app/"})
    
    (defmethod aum/config :dev [_]
      {:timbre-level :info
       :frontend-config-keys [:app-path :timbre-level]})
```

Config keys need to be assigned scalar values (so no maps or vectors) so we can
set them in env vars on the command line.

You pass the namespaces these methods are defined to aum (app.config). Any
config defined in :common will be merged with config for the current environment
with the latter overriding keys in the former. This config is then used in aum
and can be requested from aum (aum.core/get-config).

Frontend config works the same way. Keys listed in the backend under
:frontend-config-keys will be sent to the frontend and merged into the frontend
config before the app starts.


<a id="org0fa9fbc"></a>

# App starting process


<a id="org480cb3f"></a>

## dev


<a id="orgecd29c9"></a>

### Backend

When calling bin/dev-backend the last plugin (rebel) is configured in
resources/revold.edn under the :revolt.plugin/rebel.init-ns to load the clj.user
ns.
In clj.user a restart fn is defined that inits aum, inits a integrant system
with it and then calls (dev/go) on it. This kicks of all the init-key fns in the
various namespaces (db, server etc).


<a id="org604d6a2"></a>

### Frontend

When starting figwheel (by either bin/dev-figwheel or space-m-&ldquo; in Emacs) the
compiler options in dev.cljs.edn get used to produce the js from cljs. The :main
options is set to cljs.user. This is the first file loaded in the frontend by
goog.require and all the dependent files are loaded after that. So cljs.user
should require app.frontend.core. This is enough to get the app going.


<a id="org538b212"></a>

## prod

TODO-doc: explain how app starts in production


<a id="orged0fbfd"></a>

# Generic save records

When you have a page with records including their joins recursively you might
want to save the whole lot in one hit. aum calculates the actual
modifications, and only sends what&rsquo;s changed to the backend. The backend then
will save these records in the right order, taking into account newly created
records and any dependencies on them and will if anything went wrong with
updating a record return this info per record. It garantuees to leave the db in
a consistent and validated state and returns enough information so the frontend
can correct any optimistically updates to its own app state and make sure it&rsquo;s
stays in sync with the backend.


<a id="orge08dc52"></a>

# Have backend return calculated data

There are three ways to do this:


<a id="orgd61e8e5"></a>

## Calculate something over a (sub)query

Sometimes you want something to be calculated over a query and return not only
the rows themselves, but also the extra data, such as total count. This is
particularly tricky if you want to calculate something over a join. You want
the joined rows, but also some more data over that particular subset of rows
(joined as they are to the parent record).

To do this add a :with-meta param key to the params of the query. Set this to a
single keyword or map or a vector of them. If it&rsquo;s a map it should have at
least a key :type, but you can then add more params for the calculation if you
want.

You can then extend the calc-meta-data multimethod from
aum.parser.calc-meta-data in the backend which is dispatched on those
:with-meta keys, or the :type value if it&rsquo;s a map. The method is called after
the original sql query has been done. The sql-fn called, its args and
calc-params as passed from the frontend.

    [{:group [({:user [:id :name]} {:with-meta [:count {:type :calc2 :some :params}]})]}]

    (defmethod calc-meta-data :count
      [env rows {:keys [sql-fn sql-fn-args return-empty-vector? join-type calculation-params]}]
      ;;Do your calculation here
       )

One thing to take note of is that the return value for this query will be now of
the form:

    {:rows [[:id 1 :name "foo"]] :meta {:count 123}}

Which means you will have to take this into account when this data arrives at
your component, and/or when you implement the read method for the join with the
:with-meta param.


<a id="orgf882745"></a>

## Define a read key in the backend

Such as:

    (defmethod aum/read :calc/count
      [{:keys [user state parser query parser-config] :as env} _
       {:keys [table where] :as params}]
      ;;You can use the query to decide on what to calculate perhaps
      (timbre/info query) ;;=> [:count]
      {:value {:count (count-records env params)}})

Then add a query to a component:

    ({:calc/count [:count]} {:table :user
                             :where [:id :< 5]})

Disadvantage of this method is that you can only use this query as a root query
or quasi root query. Also you have to possibly duplicate the params of this query in the
frontend from another query. And this isn&rsquo;t useful for a joined query.


<a id="org82d87f8"></a>

## Redirect a read to a custom-read

Used search translations. Idea is to set a :custom-read key in the params of a
query. Backend will use the read method as set to the :custom-read key and pass
in the rest of params as well.

Advantage of this is that you can redirect a query for a join to your own read
method. Where you can then return a calculated value, any rows queried for
and/or any other data you like.

    (defmethod aum/read :count-records
      [{:keys [user state parser query parser-config] :as env} _
       {:keys [table where] :as params}]
      {:value (count-records env params)})

With this query:

    '({:user-count [:count]} {:custom-read :count-records
                              :table :user
                              :where [:id :< 5]})


<a id="orgd47bd3d"></a>

# Use datomic pull syntax to query mysql database

Use om-next queries to do crud on any mysql database, where the read can
use one or more joins from and to any table, constrained only by the
(db-)configuration of the parser.


<a id="org401697e"></a>

# Modules


<a id="orgc8c8b97"></a>

## icons

Icon classes like icon-cached, icon-undo, icon-redo etc are  defined in
mui-icons.css

This is a generated file on
<https://icomoon.io/app/#/select>

Click &ldquo;Import icons&rdquo; and select icomoon.svg in the
aum/resources/admin<sub>new</sub>/fonts directory. This adds currently used icons in the
app to the selectable icons. Select all imported icons.

Select any extra icons you want and then click &ldquo;Generate Font&rdquo;. It exports a zip
file which includes currently used icons in app, plus any other you&rsquo;ve addded..

Put the files in the fonts directory in aum/resources/admin<sub>new</sub>/fonts,
replacing the files that are already there.

Replace the contents of mui-icons.css with the css in style.css.


<a id="orge323848"></a>

## Security

There&rsquo;s login/logout methods in app/security.cljc. Disabled in production.

Aum comes with login and logout fns for both front and backend. However in
production this is disabled and users are directed to the rails app.
The remember token as set by the rails app is used to authenticate the session
similar to how it&rsquo;s done in the rails app. One complication is that because how
sente/websockets work is that to renew the session and any attached remember
token the connection has to be renewed.


<a id="org90cdb43"></a>

## Internationalization

There is a common.i18n.cljc namespace which provides the translate fn which
takes the current locale and a key.


<a id="org54ec75b"></a>

# Websockets


<a id="org3aa050a"></a>

# Database


<a id="org232e8e1"></a>

## Write validation

A generic sql query fn that garantuees validation (doesn&rsquo;t work if not
implemented) of the query with hooks for pre processing the params of the query
and post processing of the result of the query.


<a id="orgcdfb569"></a>

## Sql validation

Every call to the sql fn in the database.query ns by default is validated by
calling the aum validate-sql-fn multimethod. This dispatches on sql fn
keyword. For all mutating sql queries as defined in the aum.database.queries
ns the proper validation fn is retrieved using security/get-validation-fun.
This can be set in the database.config but if not the multimethod
aum.database.validate.core/validate multimethod is called, dispatching on
role of the user, method (sql fn keyword) and table.

Idea is that for every hugsql fn added you will have to write a validate-sql-fun
 method otherwise it will just throw an exception when its called through
 database.query/sql. You can write an empty method, and then no validation is
 done. You can do validation right there and then, or you can retrieve an
 appropriate validation fn by calling security/get-validation-fun. You will
 probably wil have to add a fn to database.config or add an appropriate
 aum.database.validate.core/validate method. Otherwise, again, an exception is
 thrown by default.


<a id="org3a7c9be"></a>

## Sql process-params, process-result

In essence all the database.query/sql fn does is first call
aum-process-params, then process-params on the params, call validate-sql then
call the actual hugsql fn and then call aum-process-result and then
process-params on the result.

aum-process-params does some built-in params processing, same for
aum-process-result. Custom versions of these fns will be used if set in the
sql prop of env.

process-params does nothing by default, process-result just returns result as
passed in.

aum.database.queries ns is used to resolve the hugsql fn

It&rsquo;s also possible to add an extra hugsql ns for resolving the sql fn.
(aum-)process-params, (aum-)process-result and validate-sql-fun are all
multimethods so you can add methods to deal with any extra hugsql fns.

process-params (and process-result) is handy for adding hooks. For instance for
the event-store. For more detail see also doc string of database.query/sql fn.


<a id="orgeded58f"></a>

# Frontend


<a id="org6a1cbfa"></a>

## make-cmp and om-data


<a id="org9383bcc"></a>

## Use pages to organize your ui

There are some basic fns for this. See app.pages for how to add a page.


<a id="org2e18643"></a>

## Client only keys

Any key with a namespace that starts with :client will never be sent to the
backend. The value for any key with the namespace :client will be looked up in
the root of app state.


<a id="orgc37ab01"></a>

## Validation of form values

When doing a save of a record on a particular page aum looks in the app
config for that page a validation function for every prop of the record. If any
prop is not &rsquo;valid&rsquo; it&rsquo;s added to the client/invalidated-fields map of the state
for that page (under the table key for that record). This can be queried for in
the relevant component and used to set any ui flags and/or messages for that
field.

TODO:
Currently this happens when a record gets saved, but it&rsquo;s possible to add a
mutation that does this on demand, for instance on onBlur..


<a id="org0cd26cf"></a>

## Syncing of front and backend

All records have as their meta data something like this:

    {:record {:id 1 :type :foo :name "bar"} ;;record as it came from the servr
     :uuids [] ;;history keeping
     :prev-uuid nil}

The meta record map is nil unless something has been modified in the record
itself. The various uuid keys are used for undo/redo functionality. They are
references to a particular state in the history of states for the app as kept by
om-next.

Reverting a record is as easy as replacing with its meta record. Calculating
what has changed to a record for purposes of sending modification to the backend
is doing a diff. And to decide whether its &rsquo;dirty&rsquo; aum in essence just
does a comparison.

It&rsquo;s possible for example to reset just the one prop of a record as a result of
clicking a &rsquo;reset&rsquo; button in the component for that field. The original value
can always be fetched from the meta record.


<a id="org3003cf1"></a>

## Generic recursive read with hooks


<a id="orgca2628e"></a>

### Intro

1.  Combining queries

    In om-next the root query is composed of sub queries recursively as they&rsquo;re
    pulled from components. However not every component necessarily represents a
    database row, or sequence of rows of a database table. Sometimes a component is
    just a grouping of other components. These components still need their own
    queries. A natural way of doing that is to use placeholder keys. Both front and
    backend parsers skip over these keys and just keep parsing and trying to return
    values for deeper lying keys instead. In the case of the backend if a key is not
    a table as set in the database config it will ignore it. In the frontend the
    parser just grabs the value of the key if it exists in the app state and keep
    parsing.

2.  Finetuning parser result

    In om-next for every render the complete root query is applied over the
    app-state (basically the same as the om-next function db->tree). This works fine
    for a small and simple app, however as an app gets more complicated you would
    like to have a bit more control of what gets returned for a key and/or if a key
    is included in any remote query. A standard om-next parser only implements
    reading the root query keys. In other words, it&rsquo;s not recursive. The aum
    parser recursively tries to interprete a query and will call any hooks for keys
    if they exist. So at any time during the parsing of a query you can insert your
    own code for resolving values and any remote. If you want to keep resolving any
    deeper lying queries you can call the supplied db->tree passed in the env
    (similar to how you received the parser in standard om-next).
    
    Standard om-next has something like dynamic queries. This extends this idea by
    letting you respond to app-state changes and changing what gets returned for any
    key anywhere in a query for both value and any remote. For instance you can set
    the selected-id in app state to 123 and in the query for your record in your
    &rsquo;selected-item&rsquo; component adding the right parameters to the query that goes to
    the backend. This should return the selected item once it&rsquo;s been fetched, but if
    you want you could customize that value as well, for example because you want to
    calculate a client side prop and add it to the value. Requesting and returning
    batches of items can be implemented similarly.


<a id="org1420a88"></a>

### Adding hooks for keys and joins in the root query for returning values and building remote query

1.  Principles

    The standard read method of aum is db->tree of om-next. This will return a
    tree of data by applying the root query over the app-state. The stock om-next
    db->tree fn has been extended in the following ways:
    
    1.  It&rsquo;s possible to define read methods for any key anywhere in the query. If
        you do you can then return anything you want for that key. You will get in
        the env the ast for the om-next expression (join or prop), the query if it&rsquo;s
        a join, context-data and (app-)state. Context data is the data relevant for
        the prop or join, which depends on where in the root query the key for the
        join or prop is. For instance the default way to resolve a prop is just to do
        (get context-data key). Default way to resolve a join is db->tree on the
        query and context-data (see aum.reconciler.parser.key.route and the read
        method for [:value :route/\*]).
    
    2.  The db->tree fn has been modified so that it instead of returning data it&rsquo;ll
        return the query again, but &rsquo;sparsified&rsquo; when :sparsify-query? flag is set.
        By default if any data is found that part of the query is elided. But again
        you can add read methods to determine yourself if and what should be included
        for any key in the root query. In standard om you need to return a (possibly
        modified) ast. For these aum read methods to work you return a (modified)
        query instead. Whatever you return will be included in the remote query. If
        you want to process and modify the ast you can you just do a (om/ast->query
        ast) when you&rsquo;re done editing it. You can also return true which will then
        result in the query being parsed further the standard db->tree way. Note that
        currently if the key is a prop only the truthiness of the return value is
        used. If truthy the return key is included, otherwise it isn&rsquo;t. Return the
        full query in case of a join. So for a read method for [:aum :foo] you
        return {:foo [:some :query]}. If query had params you can add them again,
        possibly modified.
    
    3.  Read method is dispatched on key, or on [target key]. Second one takes
        preference over first. In the first instance you need to return a map such as
        {:value :some-value :aum {:some-key [:some :query]}} similar to standard
        om-next read methods.

2.  Examples

    1.  VALUE example
    
        The method (note the :value in the dispatch vector):
        
            (defmethod aum/read [:value :bar] [{:keys [query context-data] :as env} key params] ...)
        
        for a app state structure like this:
        
            {:foo {:bar {:k1 1 :k2 2}}}
        
        and a root query of:
        
            [{:foo [{:bar [:k1 :k2 :k3]}]}]
        
        receives env like this:
        
            {:query [:k1 :2]
             :context-data {:k1 1 :k2 2}
             :ast {:type :join, :dispatch-key :bar, :key :bar, :query [:k1 :k2],
                   :children [{:type :prop, :dispatch-key :k1, :key :k1} {:type :prop, :dispatch-key :k2, :key :k2}]}
             ...
            }
        
        and should return for example this:
        
            {:k1 1 :k2 2}
    
    2.  REMOTE example
    
        The method (note the :aum in the dispatch vector):
        
            (defmethod aum/read [:aum :bar] [{:keys [query context-data] :as env} key params] ...)
        
        for a app state structure like this:
        
            {:foo {:bar {:k1 1 :k2 2}}}
        
        and a root query of:
        
            [{:foo [{:bar [:k1 :k2 :k3]}]}]
        
        receives env like this:
        
            {:query [:k1 :k2 :k3]
             :context-data {:k1 1 :k2 2}
             :ast {:type :join, :dispatch-key :bar, :key :bar, :query [:k1 :k2],
                   :children [{:type :prop, :dispatch-key :k1, :key :k1} {:type :prop, :dispatch-key :k2, :key :k2}]}
             ...
            }
        
        and should return for example this:
        
            {:bar [:k3]}
        
        to create a remote query like this:
        
            [{:foo [{:bar [:k3]}]}]
        
        If you want to keep the params (or add, or modify) return something like this:
        
            (cond-> {:bar [:k3]}
              (some? params (list params)
    
    3.  Routing
    
        Sometimes you would like to only load (send with the remote) a particular
        segment of a root query, for instance based on route of page that the user
        selected to display. By setting the selected page in app state you can (by using
        key inheritance and multimethods) only return a remote for a key that matches
        that page:
        
            (defmethod aum/read [:value :page/*]
              [{:keys [state default-remote context-data query db->tree] :as env} page params]
              (let [current-page (:app/page @state)]
                (when (= current-page page)
                  (db->tree env {:query query
                                 :data  context-data
                                 :refs  @state}))))
            
            (defmethod aum/read [:remote :page/*]
              [{:keys [state] :as env} page params]
              (let [current-page (:app/page @state)]
                (= current-page page)))
            
              (doseq [page [:page/some-page :page/some-other-page]]
                (aum/derive-om-query-key! page :page/*))
        
        This implements basic &rsquo;routing&rsquo;.
        
        This is
    
    4.  Pagination
    
        Set the query for the items you want to display paginated (or with infinite
        scroll) in the relevant component. This will by default fetch all available
        records (or as many as the server is willing to send in one batch). This is not
        what we want so we add a hook for the query for that component. In that query we
        add the proper params (such as limit, offset etc). These values will (should)
        have been set in app state with a mutation (triggered by a scroll or click of
        pagination button). Now only the records for a particular page are fetched. If
        we are paginating this is enough. If we are scrolling we need to &rsquo;cache&rsquo; the
        list of idents already in place for our key from a previous query. Then on read
        of that key we need to prefix the cached list of idents to the actual list of
        idents received from the backend.
    
    5.  Autocomplete
    
        Add a hook for the key for the autocomplete component. Return nil for any remote
        and it will not be added to the root remote query Once a search term is set in
        app state we adjust the query for the autocomplete component and add the right
        params (eg. {:where [:name :like &ldquo;%my search%&rdquo;]}). This will make data avaliable
        for the autocomplete component to display in its dropdown. This search term in
        app state will have to cleared when navigating away from the page otherwise it
        will be acted on again when returning to the page with the autocomplete.

3.  Notes

    -   If you set ignore-hooks? to true db->tree will function as the standard om-next
    
    db->tree, but by setting :sparsify-query? to true you can still also calculate
    the remote query.
    
    -   In aum.reconciler.parser.denormalize there&rsquo;s a comment block where you can
    
    play around with db->tree. There&rsquo;s also the try-frontend-read ns.
    
    -   To see the whole process in all its glory set timbre-level to :debug in
    
    app.config.cljs and set the chrome dev console to verbose output.
    
    \_ For read methods the parser is not available in the env, but db->tree is.
    
    Use of that is simple:
    
             (db->tree env {:query query ;;Apply this query
                            :data  data ;;to this data
                            :refs  app-data ;;looking up idents (refs) here.
                            :sparsify-query? false ;;Return the data, not a sparsified query
                            :ignore-hooks? false
        })


<a id="org08c33e1"></a>

## Garbage collection

There is currently no garbage collecting implemented. As with any garbage
collection the criteria for this are rather app and platform specific. But in
principle you will only have to delete any data from app state and if the ui
gets in a state where it requires that data it will just be added to any remote
query again.

A history of all app-state is kept, this is limited to 100 by default. This
could be reduced. On page change you could just wipe any idents referred to
by that page.


<a id="orgfe70b0c"></a>

## Internationalization

There is a common.i18n.cljc namespace which provides the translate fn which
takes the current locale as passed into components as a computed property and a
key.


<a id="org5efd421"></a>

## Post remote

Sometimes you would like to a take some extra action <span class="underline">after</span> a remote mutation has
finished and the data has been returned. For every mutation method you can
define a same name post-remote method. This is called with the value as returned
from the backend. Here you can do error handling for instance or &rsquo;clean up&rsquo; the
response before it get merged with app state.


<a id="org5d93ee7"></a>

## Pre-merge hooks

These hooks allow you to take action before <span class="underline">any</span> value gets merged with
app-state, including responses to read queries.


<a id="orgf1b5913"></a>

## Merging pushed data

Backend can use websockets for resolving queries from the frontend, but this
means it&rsquo;s also possible to &rsquo;push&rsquo; data. The frontend can  respond to this and
merge this as any regular response to a query. This is useful to keep instances
of the app in sync, but also to show notifications, or to push a response of a
query in an async manner. It can be sent to the frontend if and whenever the
required data is available.


<a id="org4c022d0"></a>

## Generic undo/redo/revert.

Every mutation on a record adjust some metadata on the record that enables
undo/redo/revert for that record. This also includes any data joined to that
record, they will also get undone/redone/reverted.


<a id="org926eba5"></a>

## Run backend in frontend (for testing for example)

It is possible to run the whole backend in the frontend where the mysql database
is &rsquo;mocked&rsquo; in the frontend. This is ideal for writing integration tests
covering the whole stack


<a id="orga6e5d53"></a>

## Test runner

Standalone client-side om-next test-runner app to be used with the
alternative test macros that add and remove tests to the lists of tests. Several
ways to display diffs. Rerun test on click. Use snapshots for any test instead
of writing the required result into the test. Helpers to click and compare html
output for acceptance ui tests. Replay/rewind/step through (ui) tests by using
pause macro.


<a id="orgca42ee9"></a>

## Snapshot testing

There are facilities to create a test by putting it together step by step and
instead inserting expected results take snapshots and use them instead. This is
particularly handy for testing states of the ui. It&rsquo;s also then possible to step
through the test in the test runner. If any intermediate snapshot fails the test
but (because we updated the code for example) is what we do expect we can update
the snapshot by clicking a button.


<a id="org90464b7"></a>

## Whole stack testing

By combining test runner, snapshot testing and running backend in frontend it&rsquo;s
possible to do whole stack testing.


<a id="org6be0b67"></a>

## Inspector

Search, filter and drill into app state.


<a id="org279a68f"></a>

## Dev-cards

Switch to dev cards page from app itself.


<a id="org1035e35"></a>

# Testing

First install nvm (node version manager).

Then

nvm install
nvm use
npm install
npm install -g karma-cli

TODO


<a id="orgc812736"></a>

# Debug production/staging

It&rsquo;s possible to set some flags in local storage to get some output in console
etc:

Set log level:

:timbre-level :info

Click on AUM logo and some debug buttons will show up:

:debug-drawer true

Show what query is sent and what is returned:

:send true

Show item id in lists:

:display-item-id true

Show debug buttons in page bar:

:debug-buttons true

In boot-scripts there&rsquo;s tail.boot to inspect logstash output:

boot boot-scripts/tail.boot -h

Options:
  -h, &#x2013;help        Print this help info.
  -f, &#x2013;follow      follow
  -s, &#x2013;start VAL   VAL sets start (line number or time (hh:mm) such as &ldquo;11:10&rdquo;).
  -n, &#x2013;length VAL  VAL sets number of lines or length of time such as &ldquo;10h&rdquo;, &ldquo;5m&rdquo; &ldquo;50s&rdquo; If start is given then last so many lines or within last so much time.
  -t, &#x2013;http-log    print http output lines
  -i, &#x2013;timestamp   print timestamps
  -r, &#x2013;regex VAL   VAL sets regex to filter lines.
  -l, &#x2013;level VAL   VAL sets level to filter such as info or error.


<a id="orga67d060"></a>

# Misc


<a id="org19e36c9"></a>

## Querying other sources than a mysql database


<a id="orgee528c3"></a>

### Using more than one remote in the frontend


<a id="org416f63d"></a>

### Returning data fetched from another source asynchronously

If a backend query can&rsquo;t be resolved and returned synchronously it&rsquo;s possible to
push the result to the frontend when it&rsquo;s available.


<a id="orgacb9f79"></a>

# config

-   when running bin/dev or boot dev in the dev-task there is
     (environ :env {:clj-env &ldquo;dev&rdquo;})
    This sets an env var which is picked up in app.environment. app.environment
    defines a fn that returns current environment. Which is called in app.config
    to decide on which config (dev-config, prod-config, staging-config or
    test-config) is used to build app.config/config var. The various configs have
    for ease of use a :clj-env key naming for what environment the config is for.
    
    As explained in the app.config ns itself, any env variable set on command line
    or set in profile.boot (using environ lib) will override any hardcoded setting
    in app.config. For this reason any keys in any config map will have to be
    scalar values. Because bash env vars are scalar values (numbers, strings etc).
    
    Of course when config map actually gets defined it&rsquo;s possible to build up
    submaps to be used in the app.
    
    When starting up a jar (eg bin/test-prod-jar) you will need to set the clj-env
    environment variable. There&rsquo;s a (environ :env {:clj-env &ldquo;prod&rdquo;}) in the build
    task, but this has only effect on the build. Not the running of the program
    (when running the jar).
    
    Require app.config if you need settings [app.config :refer [config]]. However
    in om parser read and mutate methods the config is part of the env param
    passed in as :parser-config. Better to use that so it can be more easily
    mocked in tests.
    
    At top of app.config ns there is env-keys defined. This is a set of all
    settings that can be overridden/set on the commandline or profile.boot.


<a id="org6f99536"></a>

# (sql) validation

Every call to the sql fn in the database.query ns by default is validated by
calling the aum validate-sql-fn multimethod. This dispatches on sql fn
keyword. For all mutating sql queries as defined in the aum.database.queries
ns the proper validation fn is retrieved using security/get-validation-fun.
This can be set in the database.config but if not the multimethod
aum.database.validate.core/validate multimethod is called, dispatching on
role of the user, method (sql fn keyword) and table.

Idea is that for every hugsql fn added you will have to write a validate-sql-fun
 method otherwise it will just throw an exception when its called through
 database.query/sql. You can write an empty method, and then no validation is
 done. You can do validation right there and then, or you can retrieve an
 appropriate validation fn by calling security/get-validation-fun. You will
 probably wil have to add a fn to database.config or add an appropriate
 aum.database.validate.core/validate method. Otherwise, again, an exception is
 thrown by default.


<a id="org57689f1"></a>

# sql process-params, process-result

In essence all the database.query/sql fn does is first call
aum-process-params, then process-params on the params, call validate-sql then
call the actual hugsql fn and then call aum-process-result and then
process-params on the result.

aum-process-params does some built-in params processing, same for
aum-process-result. Custom versions of these fns will be used if set in the
sql prop of env.

process-params does nothing by default, process-result just returns result as
passed in.

aum.database.queries ns is used to resolve the hugsql fn

It&rsquo;s also possible to add an extra hugsql ns for resolving the sql fn.
(aum-)process-params, (aum-)process-result and validate-sql-fun are all
multimethods so you can add methods to deal with any extra hugsql fns.

process-params (and process-result) is handy for adding hooks. For instance for
the event-store. For more detail see also doc string of database.query/sql fn.


<a id="org15f5483"></a>

# Read permissions and create/update/delete permissions, and validations of om-queries

These are set in database.config namespace.


<a id="orge92076a"></a>

# frontend testing

-   Run
    npm install

in aum dir

-   Run
    npm install -g karma-cli

Browser in memory sql options:
<https://github.com/kripken/sql.js>
<https://github.com/agershun/alasql/wiki/Getting%20started>

parser.core is now a cljc file, including all its deps


<a id="orga1f2b4e"></a>

# Deciding on selected group

The app can be in a state where a group is &rsquo;selected&rsquo;. In this state certain
pages (like users, translations) will manage records only from/for the selected
group. Some pages are immuun ie, they behave the same regardless of selected
group, like groups page itself, or job offers, or support questions. Other pages
only can only edit records of a particular group, like dossier types, pdf
options.

By default a selected group is the current user&rsquo;s group. But it can also be
set/derived from local/session storage (or from any state in the url
(unimplemented as of 7/18)). The app can be in a state of &rsquo;all groups&rsquo; by
setting selected group-id to -1 or nil.

Complication is that on refresh, while we&rsquo;re logged in, we don&rsquo;t know what the
current user&rsquo;s group is since we don&rsquo;t have that info yet. One
massive query goes to the backend asking for the current user&rsquo;s data, and any other
data required for the current page. Solution for this is not to ask for any
specific group-id number, but for a property on the current user, so for
u/group-id in this case.

The backend resolves what user is actually making the massive initial query
before parsing the actual query itself. (This enables role based access, scoping
etc) The user is passed into the query parser, and any params that are
namespaced keywords are resolved against the user&rsquo;s map first.

We need to weave this variable group-id into the queries that go to the backend.
We&rsquo;re not using om-next dynamic queries at all, but instead give parameters to
query keys that are picked by the cljs query parser. These parameters are like
{:params :selected-group} for instance. The parser goes and looks for the
:selected-group entry in the :params value of the config for the current page.
This can be a map, in which case this is used as the params map for the key in
the query, or a fn. This fn is called with app-state and the result is used as
the params for query key.

All this is not very standardized actually, and there&rsquo;s parallel mechanisms
currently. We have one for batch queries: :batch-params and one for single
record queries: :params. Under a table entry for a page-config we have similar
entries for deciding on what remote keys to send (:selected-remote-keys and :batch-remote-keys).

In any case, initial group-id is set in reconciler.app-state, per page, where
it&rsquo;s usually set to whatever is :selected-group in storage, or if that&rsquo;s
desirable, u/group-id, meaning the user&rsquo;s group-id.


<a id="org3f0f56a"></a>

# Trying queries

In the dev source folder there are namespaces to try out various queries:


<a id="orgf8d103a"></a>

## try-om-query

You can call the backend parser with any om-next query. These are resolved
against the database as defined in app.config and using database.config as
defined for the whole app.

There is a second version where you can build your own parser environment and
your own parser with that again.


<a id="org85cd89d"></a>

## Try sql query

To try out any sql query. Make sure to define process-params, validate-sql-fn
and process-result methods, and the equivalent sql fun in build-sql if you want
it to be used in mock mode or tests.


<a id="org0900db2"></a>

## Try/test frontend parser.

Frontend parser is a cljc file so you can eval this in a clojure repl. You can
test here what the parser returns for queries for the nil and various remote
targets, which is much harder to test/inspect if you have to use the ui to pass
queries to the parser.


<a id="org7d81c71"></a>

# Start aum with different ports and db:

DB<sub>NAME</sub>=chin<sub>dev</sub><sub>minimal</sub> SERVER<sub>PORT</sub>=9080 NREPL<sub>PORT</sub>=38401 RELOAD<sub>PORT</sub>=46501 bin/dev


<a id="org1969239"></a>

# pathopt

  <https://awkay.github.io/om-tutorial/#!/om_tutorial.I_Path_Optimization>
  Path Optimization
As your UI grows you may see warnings in the Javascript Console about slowness.
If you do, you can leverage path optimization to minimize the amount of work the
parser has to do in order to update a sub-portion of the UI.

If you pass :pathopt true to the reconciler, then when re-rendering a component
that has an Ident Om will attempt to run the query starting from that component
(using it&rsquo;s Ident as the root of the query). If your parser returns a result, it
will use it. If your parser returns nil then it will focus the root query to
that component and run it from root.

When it attempts this kind of read it will call your read function with
:query-root set to the ident of the component that is needing re-render, and you
will need to follow the query down from there. Fortunately, db->tree still works
for the default database format with a little care.

So om-next calls the parser, but the query will be a (focussed on the cmp) query
against the root of app-data. If you set pathopt to true and a cmp has an ident
and a query it will call the parser with the :query-root key of env to the
ident, and query to the query of the cmp, so the parser can work a bit faster.
Which I do in my parser read\* fn


<a id="org66117c9"></a>

# Adding hooks for keys and joins in the root query for returning values and building remote query


<a id="orga1e721b"></a>

## Principles

The standard read method of aum is db->tree of om-next. This will return a
tree of data by applying the root query over the app-state. The stock om-next
db->tree fn has been extended in the following ways:

1.  It&rsquo;s possible to define read methods for any key anywhere in the query. If
    you do you can then return anything you want for that key. You will get in
    the env the ast for the om-next expression (join or prop), the query if it&rsquo;s
    a join, context-data and (app-)state. Context data is the data relevant for
    the prop or join, which depends on where in the root query the key for the
    join or prop is. For instance the default way to resolve a prop is just to do
    (get context-data key). Default way to resolve a join is db->tree on the
    query and context-data (see aum.reconciler.parser.key.route and the read
    method for [:value :route/\*]).

2.  The db->tree fn has been modified so that it instead of returning data it&rsquo;ll
    return the query again, but &rsquo;sparsified&rsquo; when :sparsify-query? flag is set.
    By default if any data is found that part of the query is elided. But again
    you can add read methods to determine yourself if and what should be included
    for any key in the root query. In standard om you need to return a (possibly
    modified) ast. For these aum read methods to work you return a (modified)
    query instead. Whatever you return will be included in the remote query. If
    you want to process and modify the ast you can you just do a (om/ast->query
    ast) when you&rsquo;re done editing it. You can also return true which will then
    result in the query being parsed further the standard db->tree way. Note that
    currently if the key is a prop only the truthiness of the return value is
    used. If truthy the return key is included, otherwise it isn&rsquo;t. Return the
    full query in case of a join. So for a read method for [:aum :foo] you
    return {:foo [:some :query]}. If query had params you can add them again,
    possibly modified.

3.  Read method is dispatched on key, or on [target key]. Second one takes
    preference over first. In the first instance you need to return a map such as
    {:value :some-value :aum {:some-key [:some :query]}} similar to standard
    om-next read methods.


<a id="org3fdd081"></a>

## Examples


<a id="org4431aad"></a>

### VALUE example

The method (note the :value in the dispatch vector):

    (defmethod aum/read [:value :bar] [{:keys [query context-data] :as env} key params] ...)

for a app state structure like this:

    {:foo {:bar {:k1 1 :k2 2}}}

and a root query of:

    [{:foo [{:bar [:k1 :k2 :k3]}]}]

receives env like this:

    {:query [:k1 :2]
     :context-data {:k1 1 :k2 2}
     :ast {:type :join, :dispatch-key :bar, :key :bar, :query [:k1 :k2],
           :children [{:type :prop, :dispatch-key :k1, :key :k1} {:type :prop, :dispatch-key :k2, :key :k2}]}
     ...
    }

and should return for example this:

    {:k1 1 :k2 2}


<a id="org736fa5d"></a>

### REMOTE example

The method (note the :aum in the dispatch vector):

    (defmethod aum/read [:aum :bar] [{:keys [query context-data] :as env} key params] ...)

for a app state structure like this:

    {:foo {:bar {:k1 1 :k2 2}}}

and a root query of:

    [{:foo [{:bar [:k1 :k2 :k3]}]}]

receives env like this:

    {:query [:k1 :k2 :k3]
     :context-data {:k1 1 :k2 2}
     :ast {:type :join, :dispatch-key :bar, :key :bar, :query [:k1 :k2],
           :children [{:type :prop, :dispatch-key :k1, :key :k1} {:type :prop, :dispatch-key :k2, :key :k2}]}
     ...
    }

and should return for example this:

    {:bar [:k3]}

to create a remote query like this:

    [{:foo [{:bar [:k3]}]}]

If you want to keep the params (or add, or modify) return something like this:

    (cond-> {:bar [:k3]}
      (some? params (list params)


<a id="orgf91311e"></a>

## Notes

-   If you set ignore-hooks? to true db->tree will function as the standard om-next

db->tree, but by setting :sparsify-query? to true you can still also calculate
the remote query.

-   In aum.reconciler.parser.denormalize there&rsquo;s a comment block where you can

play around with db->tree. There&rsquo;s also the try-frontend-read ns.

-   To see the whole process in all its glory set timbre-level to :debug in

app.config.cljs and set the chrome dev console to verbose output.

\_ For read methods the parser is not available in the env, but db->tree is.

Use of that is simple:

         (db->tree env {:query query ;;Apply this query
                        :data  data ;;to this data
                        :refs  app-data ;;looking up idents (refs) here.
                        :sparsify-query? false ;;Return the data, not a sparsified query
                        :ignore-hooks? false
    })


<a id="orgfc1a6f7"></a>

# Have backend return calculated data

There are three ways to do this:


<a id="orgc2accf8"></a>

## Calculate something over a (sub)query

Sometimes you want something to be calculated over a query and return not only
the rows themselves, but also the extra data, such as total count. This is
particularly tricky if you want to calculate something over a join. You want
the joined rows, but also some more data over that particular subset of rows
(joined as they are to the parent record).

To do this add a :with-meta param key to the params of the query. Set this to a
single keyword or map or a vector of them. If it&rsquo;s a map it should have at
least a key :type, but you can then add more params for the calculation if you
want.

You can then extend the calc-meta-data multimethod from
aum.parser.calc-meta-data in the backend which is dispatched on those
:with-meta keys, or the :type value if it&rsquo;s a map. The method is called after
the original sql query has been done. The sql-fn called, its args and
calc-params as passed fromt the frontend.

    [{:group [({:user [:id :name]} {:with-meta [:count {:type :calc2 :some :params}]})]}]

    (defmethod calc-meta-data :count
      [env rows {:keys [sql-fn sql-fn-args return-empty-vector? join-type calculation-params]}]
      ;;Do your calculation here
       )

One thing to take note of is that the return value for this query will be now of
the form:

    {:rows [[:id 1 :name "foo"]] :meta {:count 123}}

Which means you will have to take this into account when this data arrives at
your component, and/or when you implement the read method for the join with the
:with-meta param.


<a id="org68a2fa1"></a>

## Define a read key in the backend

Such as:

    (defmethod aum/read :calc/count
      [{:keys [user state parser query parser-config] :as env} _
       {:keys [table where] :as params}]
      ;;You can use the query to decide on what to calculate perhaps
      (timbre/info query) ;;=> [:count]
      {:value {:count (count-records env params)}})

Then add a query to a component:

    ({:calc/count [:count]} {:table :user
                             :where [:id :< 5]})

Disadvantage of this method is that you can only use this query as a root query
or quasi root query. Also you have to possibly duplicate the params of this query in the
frontend from another query. And this isn&rsquo;t useful for a joined query.


<a id="org4fb8e73"></a>

## Redirect a read to a custom-read

Used search translations. Idea is to set a :custom-read key in the params of a
query. Backend will use the read method as set to the :custom-read key and pass
in the rest of params as well.

Advantage of this is that you can redirect a query for a join to your own read
method. Where you can then return a calculated value, any rows queried for
and/or any other data you like.

    (defmethod aum/read :count-records
      [{:keys [user state parser query parser-config] :as env} _
       {:keys [table where] :as params}]
      {:value (count-records env params)})

With this query:

    '({:user-count [:count]} {:custom-read :count-records
                              :table :user
                              :where [:id :< 5]})


<a id="org4727581"></a>

# invalidation

On save of eg a dossier type:
(bu/get-key-in-page-state @state :dossier-type :validate)
invalidated-fields (bu/calc-invalidations dossier-type validate)

(if (seq invalidated-fields)
  (bu/set-key-in-page-state state :dossier-type :invalidated-fields invalidated-fields))

So on save you fetch validate map for the relevant record type
You give the record and the validate map to calc-invalidations

For every key in record calc-invalidations calls the validated? fn of the value
map of the same key in the validate map and sets the [:invalidated? :prop] key in the
validate map to true and returns it.

So in page-state:

    {:route/dossier-types {:table {:dossier-type {:validate {:name validate-name-map
                                                             :some-other-prop validate-some-other-prop}
                                                   :invalidated-fields {:name {:invalidated? {} :message ""}
    }}}}

You then set a key called :invalidated-fields in page state to that validate
map. Which you can pick up in your components and use it to modify the ui if
needed (show in red, show error message etc)

