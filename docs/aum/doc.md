
# Table of Contents

1.  [Getting started](#org1f0f52d)
    1.  [Useful docs to read first](#org2fa6ee6)
        1.  [React](#orgadda168)
        2.  [Om-next](#orgf6221f0)
        3.  [Fulcro handbook](#org5fd3a30)
        4.  [See om-next docs for more useful links](#org533d643)
    2.  [Install](#org708e56a)
    3.  [Starter app](#orgdcce52a)
        1.  [Install](#org09ac522)
        2.  [Run in development mode](#orga4dddb6)
        3.  [Run in production mode](#orge7d9048)
2.  [Core concepts](#orgfe9a098)
    1.  [It&rsquo;s om-next.](#orgf5097cf)
    2.  [It&rsquo;s a loop](#org91ec03c)
    3.  [It&rsquo;s just a sql query](#org49ed247)
    4.  [Communication between front and backend is through websockets](#org32180a5)
    5.  [We pretend we have all data already in the frontend](#org17fc873)
    6.  [Mutating queries, so transactions mostly work the same as in om-next](#org4dfaeaf)
    7.  [Backend state management is handled by integrant.](#org453df14)
3.  [Build system](#org9319d15)
4.  [App starting process](#org62fa1ab)
    1.  [dev](#org25a82ba)
        1.  [Backend](#orgf60048c)
        2.  [Frontend](#orga80ad6f)
    2.  [prod](#orgd0c838d)
        1.  [Backend](#org7106ed1)
        2.  [Frontend](#orgd2f01f6)
5.  [Environment](#org02947ac)
6.  [Config](#org7eb551b)
7.  [Websockets](#orgb9307fe)
8.  [Database](#org9a35ebd)
    1.  [(sql) validation](#orged2e7c4)
    2.  [sql process-params, process-result](#orga76926a)
    3.  [Write validation](#orgc2a7706)
    4.  [Sql validation](#org015fa3f)
    5.  [Sql process-params, process-result](#org268fa06)
9.  [Frontend matters](#orgfd4d6e0)
    1.  [Generic recursive read with hooks](#orgc23d96b)
        1.  [Intro](#org5db25a7)
        2.  [Adding hooks for keys and joins in the root query for returning values and building remote query](#orgd110c48)
    2.  [Client only keys](#org407b039)
    3.  [make-cmp and om-data](#orgb5d1611)
    4.  [Garbage collection](#org9472fa0)
    5.  [Internationalization](#orgd1965ed)
    6.  [Pre-merge hooks](#orga133902)
    7.  [Merging pushed data](#orgb886835)
    8.  [Generic undo/redo/revert.](#orgf1d5bac)
10. [Backend parser](#orgc13b88e)
    1.  [Table aliases](#orgc30910c)
    2.  [Virtual tables](#org043f90c)
    3.  [Have backend return calculated data](#org38a0369)
        1.  [Calculate something over a (sub)query](#org9c01444)
        2.  [Define a read key in the backend](#orgcc756c5)
        3.  [Redirect a read to a custom-read](#org4b20fa4)
11. [Testing](#org61d2c55)
    1.  [Backend testing](#org7acbaec)
    2.  [Frontend testing](#orgb78f0a5)
        1.  [Install](#org5deb589)
        2.  [Test runner](#org92c6822)
        3.  [Snapshots](#org73a4eef)
    3.  [Run backend in frontend (for testing for example)](#orgef3316e)
    4.  [Whole stack testing](#org04f6bb6)
12. [Debugging](#org6313c2c)
    1.  [Dev-cards](#org2ae15c4)
    2.  [Frontend om inspector](#org0456b7d)
    3.  [It&rsquo;s possible to set some flags in local storage to get some output in console etc:](#orgf4364a5)
        1.  [Trying queries](#org2ceb406)
    4.  [In boot-scripts there&rsquo;s tail.boot to inspect logstash output:](#org8be04f8)
13. [How to](#org8b859e9)
    1.  [Add npm modules](#org1a9f653)
    2.  [Querying other sources than a mysql database](#org26ce28f)
        1.  [Using more than one remote in the frontend](#orgff77073)
        2.  [Returning data fetched from another source asynchronously](#orgaf5cb11)
    3.  [Optimize frontend](#org9d25606)
        1.  [pathopt](#org51eb643)
    4.  [Start app with different ports and db:](#org2dac1e5)
    5.  [throw catch exceptions](#org51b3ba4)
14. [Good to know](#orgcda28d4)
    1.  [Function and method signatures](#org504d54b)
        1.  [read and mutate: [env key params] TODO-doc: update for aum](#orgc10c223)
    2.  [Syncing of front and backend](#orgdaa4947)
15. [Modules](#orgb0485c3)
    1.  [Validation of form values](#orgd4f13b1)
    2.  [Use pages to organize your ui](#orgeee8992)
    3.  [invalidation](#org598e25c)
    4.  [Generic save records](#org5744549)
    5.  [Database migration](#orga11ed9a)
    6.  [Integrations](#org3e8b7eb)
    7.  [Paging](#org0a3bb49)
    8.  [Routing](#orgac3d7d7)
    9.  [Internationalization](#orge920e05)
        1.  [There is a common.i18n.cljc namespace which provides the translate fn which](#org00c3941)
        2.  [smarter translations](#orgbf118ab)
    10. [Files download and upload](#org05b5e46)
    11. [Event store](#org50cec08)
    12. [Calc active users](#org6317f39)
    13. [Icons](#org83cfc57)
    14. [Security](#org23a8ac6)
        1.  [login/logout](#org98459fc)
        2.  [bugsnag, authorization, login, logout etc](#org72528e8)
        3.  [Process-user and calc-role snippets](#orgc995b14)



<a id="org1f0f52d"></a>

# Getting started


<a id="org2fa6ee6"></a>

## Useful docs to read first


<a id="orgadda168"></a>

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


<a id="orgf6221f0"></a>

### Om-next

[https://github.com/omcljs/om/wiki/Documentation-(om.next)](https://github.com/omcljs/om/wiki/Documentation-(om.next))


<a id="org5fd3a30"></a>

### Fulcro handbook

<http://book.fulcrologic.com/>
Most of it is about how om-next itself works. The solutions for a more practical
om-next are a bit different, and in some ways diverge somewhat from the &rsquo;om-next
way&rsquo; of doing things.


<a id="org533d643"></a>

### See om-next docs for more useful links

Such as for graphql, falcor, datomic etc.

&ldquo;Om-next by leveraging lisp and immutable values combines the best ideas of
graphql and falcor in a less cumbersome and more flexible manner.&rdquo;


<a id="org708e56a"></a>

## Install

To pull in all the tools and libs to build an aum app add

<div class="center-column"></div>
```clojure
    aum {:git/url "https://github.com/michieljoris/aum.git",
         :sha "577daf362c3f81e08d43f654ef0bbf3ddc93e015"
         :tag "master"},
```

to your dependencies.

To actually build an app it&rsquo;s a good idea to start with a minimal setup, see the
following section.


<a id="orgdcce52a"></a>

## Starter app

TODO-doc: add github link to aum-starter-app

There is a repo that&rsquo;s an app using aum libs, but has minimal content. This can
be used to try out building features/pages.

Clone it and follow the following instructions.


<a id="org09ac522"></a>

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


<a id="orga4dddb6"></a>

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


<a id="orge7d9048"></a>

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


<a id="orgfe9a098"></a>

# Core concepts


<a id="orgf5097cf"></a>

## It&rsquo;s om-next.

The idea is to stay as close as possible to the original idea of om-next as just
a thin layer on the top of react, but extend some concepts so at the very least
a straightforward crud app can be built really simply and quickly, with simple
tools to facilitate both front and backends development.

Om-next itself is isomorphic, meaning it can ran on either front or backend. Aum
extends om-next, and some of it can be used on both front and backend, but in
general it focuses more on making om-next useful in a practical way.

So in the backend the om-next parser is implemented to query a mysql database,
with security and validation mechanisms.

On the frontend aum implements a parser that in most cases will do the
right thing in denormalizing queries over the app state. And there are mechanism
for dealing with multiple remotes, websockets, error handling, correcting
optimistic updates etc.

Aum has its own thin layers over the reconciler and parser but still uses defui,
om/transact! etc


<a id="org91ec03c"></a>

## It&rsquo;s a loop

Frontend gui is a tree of ui components, with at the base the root component.
Every component declares in a query the data it needs to function. Parent
components compose their queries by adding the queries of its child
components to their own query. The root component thus is composed of a query in
the form of a tree of queries covering the data needs of the tree of gui components.

This query is fed to the reconciler together with the app state atom and the
parser. The query tree is parsed and processed with the current app state as the
source of data. The resulting tree of data is then passed to react to render the
gui. Any data not found in the current app state is queried for from a remote
over the network.

When any data is returned the app state is updated, query is run against the new
app state, the updated data tree is given again to React and the gui is updated
once more.

After the initial render the gui is only updated again when app state is
modified. This can happen through user actions, but also though data that comes
in from the network, either as a response to a query, or because data is pushed
from the server to the app.

Once app state is modified, query is run again, data tree built, fed to React
which rebuilds the gui. And once again the event driven system will be waiting
for app state updates.

This is a very broad outline with many details, optimizations and nuances left out, but
is the basic concept to hold in mind when designing and debugging a gui.


<a id="org49ed247"></a>

## It&rsquo;s just a sql query

Or rather, a datomic pull query:

Currently the built in resolver for queries turns datomic pull queries into a
sql query string. This doesn&rsquo;t mean it&rsquo;s not possible to return data from
different sources but by default the assumption is that a query is mainly built
up as a nested query of joined tables, from and to any table.

Access to data is by default disallowed and an explicit configuration for every
table used needs to be defined and passed to aum before a query will return any
data. Access is role based.


<a id="org32180a5"></a>

## Communication between front and backend is through websockets

If websockets are not available connection will fall back to ajax calls and
polling. See Sente library. Most communication will be through om queries, however
it&rsquo;s possible to define custom response handlers for non-om queries on both
front and backend. Advantage of websockets is push functionality, for instance
for notifications, or even to update frontend in response to backend database
updates by other clients.


<a id="org17fc873"></a>

## We pretend we have all data already in the frontend

When defining queries for components ask for all data the component could
possibly need. The root query will be automatically resolved against the app
state and generate data for the gui tree and queries for all remotes.

However we very likely might not want to resolve a query in its totality every
time. For instance if we have more than one page we don&rsquo;t need to produce data
for pages we&rsquo;re not currently showing. Neither do we care to fetch data for
those pages. A good place to store the information on what we care about and
what not at any given moment is app state itself. For instance we might have a
key :app/page in app state.

We write multimethods that dispatch on target (value or a remote) and keyword.
For instance:

    ```clojure
    (doseq [page [:page/some-page :page/some-other-page]]
      (aum/derive-om-query-key! page :page/*))
    
    (defmethod aum/read [:value :page/*]
      [{:keys [state context-data query db->tree] :as env} page params]
      (let [current-page (:app/page @state)]
        (when (= current-page page)
          (db->tree env {:query query
                         :data  context-data
                         :refs  @state}))))
    
    (defmethod aum/read [:my-remote :page/*]
      [{:keys [state default-remote context-data query ast] :as env} page params]
      (let [current-page (:app/page @state)]
        (= current-page page)))
    ```

In standard om-next you write read methods to resolve the root keys or the root
query. In aum you write methods that allow custom resolving of keys *anywhere*
in the root query for both value and remotes.

The db->tree fn is adapted from the one from om-next and is the default read
method used in the parser. Without defining any custom aum/read methods the
db->tree fn actually behaves the same way as the standard om-next db->tree fn.

The actual read function passed to the om parser basically does this:

    ```clojure
    (db->tree env {:query root-component-query
                   :data  app-state
                   :refs  app-state})
    ```

In practice this allows us to have total control over what we return as data to
the gui component tree and what queries we send to our remotes for any key
anywhere in the root query every time we do our &rsquo;loop&rsquo;.

As in the &rsquo;routing&rsquo; example the best place to store information to &rsquo;direct&rsquo; our
read methods is in app state. To further differentiate between similarly named
keys in our root key we can wrap our query expressions with parameters (when we
define them in components).


<a id="org4dfaeaf"></a>

## Mutating queries, so transactions mostly work the same as in om-next

You either mutate frontend app-state:

    ```clojure
    (defmethod mutate 'admin/set-key
      [{:keys [state] :as env} _ {:keys [key value]}]
      {:action (fn []
                 (swap! state assoc key value))})
    
    ```

So just return a map with an action. In that action fn you have access to the
app state as an atom.

And/or you set a key that&rsquo;s a defined remote in the returned map to true:

    ```clojure
    (defmethod mutate 'app/test
      [{:keys [state]} _ {:keys [p1 p2] :as params}]
      {:my-remote true
       :post-remote {:param-keys [p1 p2]
                     :params {:p3 123}}
       :action (fn [] '...)})
    ```

You&rsquo;ll have to handle this mutation in the backend.

Sometimes however you would like to a take some extra action <span class="underline">after</span> a remote
mutation has finished and the data has been returned. For every mutation method
you can define a same name post-remote method. This is called with the value as
returned from the backend. Here you can do error handling for instance or &rsquo;clean
up&rsquo; the response *before* it get merged with app state.

    ```clojure
    (defmethod post-remote 'app/test
      [_ state
       {:keys [error keys]
        {{:keys [p1 p2 p3] :as params} :params} :post-remote
        :as value}]
      ;;Do something!!
      )
    ```

The :post-remote key in the mutation is a mechanism to pass data to the post
remote method from the mutation. TODO-aum: there might be a better mechanism for
this. At the moment this involves the backend, but it is purely a frontend
concern.


<a id="org453df14"></a>

## Backend state management is handled by integrant.

TODO-aum: add link to integrant.
TODO-aum: add a way so an app can add it&rsquo;s own state-full components.
TODO-aum: add a hook for when app is done initializing


<a id="org9319d15"></a>

# Build system

Aum itself is a tools.deps project

For compile, nrepl, and other build and developing time concerns the aum starter
app uses [revolt](https://github.com/mbuczko/revolt):

> revolt is a plugins/tasks oriented library which makes it easier to integrate
> beloved dev tools like nrepl, rebel readline or clojurescript into application,
> based on Cognitect&rsquo;s command line tools.

A slight adaption of revolt and modified and new tasks are in the pagora.revolt
repo. The source code of revolt is very readable and extendable with more tasks
and plugins.

In the bin directory of the aum starter app is a set of build and dev scripts.

Also, npm modules can be added to the the project by adding them to package.json
,webpacking them, add any externs. The whole webpacked js file is added as a
foreign lib in the compiler configuration. There&rsquo;s scripts and examples in the
repo. Trickiest might be to create an extern file, however there&rsquo;s tools to
automate that (TODO-doc links?). See later section for more details.


<a id="org62fa1ab"></a>

# App starting process


<a id="org25a82ba"></a>

## dev


<a id="orgf60048c"></a>

### Backend

When calling bin/dev-backend the last plugin (rebel) is configured in
resources/revolt.edn under the :revolt.plugin/rebel.init-ns to load the clj.user
ns.
In clj.user a restart fn is defined that inits aum, inits a integrant system
with it and then calls (dev/go) on it. This kicks of all the init-key fns in the
various namespaces (db, server etc).


<a id="orga80ad6f"></a>

### Frontend

When starting figwheel (by either bin/dev-figwheel or space-m-&ldquo; in Emacs) the
compiler options in dev.cljs.edn get used to produce the js from cljs. The :main
option is set to cljs.user. This is the first file loaded in the frontend by
goog.require and all the dependent files are loaded after that. cljs.user should
require app.frontend.core. In that namespace aum is initialized with the root
component and the initial app state. The returned aum config is then passed to
pagora.aum.frontend.core/go fn.


<a id="orgd0c838d"></a>

## prod


<a id="org7106ed1"></a>

### Backend

When building the production jar the pagora.revolt.task/capsule is used. The
configuration for that task specifies the app.core namespace as the main
namespace.


<a id="orgd2f01f6"></a>

### Frontend

When building the production jar using revolt the cljs compiler options in
resources/revolt.edn are used. In there the main key is set to
app.frontend.core, bypassing cljs.user. After that the process is the same as in
dev mode. See the bin/prod-run script for an example how to actually run the
production jar.


<a id="org02947ac"></a>

# Environment

You can require pagora.aum.environment in both front and backend. The current
environment is in the pagora.aum.environment/environment var or call functions
like is-development? from that namespace. The default environment is :dev. Start
the app with CLJ<sub>ENV</sub>=[production|staging|testing|dev] to change the environment.


<a id="org7eb551b"></a>

# Config

Config is defined in multimethods like this, for instance in app.config:

    ```clojure
    (ns app.config)
    
    (defmethod aum/config :common [_]
      {:timbre-log-level :error
       :app-path "app/"})
    
    (defmethod aum/config :dev [_]
      {:timbre-level :info
       :frontend-config-keys [:app-path :timbre-level]})
    ```

Any env variable set on command line will override any hardcoded setting in
app.config. For this reason any keys in any config map will have to be scalar
values. Because bash env vars are scalar values (numbers, strings etc).

You pass the namespaces these methods are defined to aum (eg. app.config). Any
config defined in :common will be merged with config for the current environment
with the latter overriding keys in the former. This config is then used in aum
and can be requested from aum (aum.core/get-config). Aum groups some of these
keys already (eg. under :mysql-database, :nrepl, :server), if you want to group
other keys or in general want to process the config before it gets used in the
app as returned from get-config pass a preprocess-config fn to aum. TODO-aum:
implement!

Frontend config works similarly, so also with multimethods.

Keys listed in the backend under :frontend-config-keys will be sent to the
frontend and merged into the frontend config before the app starts.

When starting up a jar you will need to set the clj-env environment variable,
also see bin/prod-run . There&rsquo;s a CLJ<sub>ENV</sub>=prod in the bin/prod-build script, but
this has only effect on the build. Not the running of the program (when running
the jar).

Call get-config if you need settings. However in aum parser read and mutate
methods the config is part of the env param passed in as :parser-config. Better
to use that so it can be more easily mocked in tests. TODO-aum: ??? is this so?

See pagora.aum.config/default-config and pagora.aum.config/parser-config for all
settings that can be overridden/set on the commandline, besides the ones as set
in app.config.


<a id="orgb9307fe"></a>

# Websockets

TODO-doc: more info on how to use websockets for any custom communication
between front and backend


<a id="org9a35ebd"></a>

# Database


<a id="orged2e7c4"></a>

## (sql) validation

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


<a id="orga76926a"></a>

## sql process-params, process-result

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


<a id="orgc2a7706"></a>

## Write validation

A generic sql query fn that garantuees validation (doesn&rsquo;t work if not
implemented) of the query with hooks for pre processing the params of the query
and post processing of the result of the query.


<a id="org015fa3f"></a>

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


<a id="org268fa06"></a>

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


<a id="orgfd4d6e0"></a>

# Frontend matters


<a id="orgc23d96b"></a>

## Generic recursive read with hooks


<a id="org5db25a7"></a>

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


<a id="orgd110c48"></a>

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


<a id="org407b039"></a>

## Client only keys

Any key with a namespace that starts with :client will never be sent to the
backend. The value for any key with the namespace :client will be looked up in
the root of app state. So for instance the key :client-prop/name will not be
sent to the backend either. But will still be looked up in the context data for
that key.


<a id="orgb5d1611"></a>

## make-cmp and om-data

Use pagora.aum.frontend.util/make-cmp function to create a function that you can
use in your render function in other components:

    ```clojure
    (defui ^:once Foo
      static om/IQuery
      (query [this]
        [:client/foo])
      Object
      (render [this]
        (html [:div "in foo"])))
    
    (def foo-cmp (make-cmp Foo)) ;;instead of (def foo (om/factory Foo))
    ```

> make-cmp:
>   Returns a fn[parent-cmp props-or-kw & computed-arg] that when called will
>   create a React element. Options can be map with :validator, :keyfn
>   and :instrument? keys. If props-or-kw is a keyword its value will be retrieved
>   from the parent-cmp props. Any reload-key from the parent-cmp will be added to
>   the computed props of this cmp. This will only happen in development.

So call this foo function with the `this` of the parent component and the
key you set the query of Foo to:

    ```clojure
    (defui ^:once RootQbucketList
      static om/IQuery
      (query [this]
        {:foo (om/get-query Foo)})
     (render [this]
       (let [{:keys [props computed state]} (om/data this)]
         (foo-cmp this :foo (assoc computed :some-key some-value)))))
    ```

It&rsquo;s first of all a bit more straightforward get a handle on the props, computed
and state values. But also the proper value is passed on to foo-cmp, less error
prone. Last benefit is that if you defined a :client/reload-key in your root
component&rsquo;s query and you make sure that the value of that key is modified on
every reload in dev mode (as a result of modifying source code) then the gui is
updated as a whole (since the reload-key is passed on to child components in the
computed value by aum).


<a id="org9472fa0"></a>

## Garbage collection

There is currently no garbage collecting implemented. As with any garbage
collection the criteria for this are rather app and platform specific. But in
principle you will only have to delete any data from app state and if the ui
gets in a state where it requires that data it will just be added to any remote
query again.

A history of all app-state is kept, this is limited to 100 by default. This
could be reduced. On page change you could just wipe any idents referred to
by that page.


<a id="orgd1965ed"></a>

## Internationalization

There is a common.i18n.cljc namespace which provides the translate fn which
takes the current locale as passed into components as a computed property and a
key.


<a id="orga133902"></a>

## Pre-merge hooks

These hooks allow you to take action before <span class="underline">any</span> value gets merged with
frontend app-state, including responses to read queries.


<a id="orgb886835"></a>

## Merging pushed data

Backend can use websockets for resolving queries from the frontend, but this
means it&rsquo;s also possible to &rsquo;push&rsquo; data. The frontend can  respond to this and
merge this as any regular response to a query. This is useful to keep instances
of the app in sync, but also to show notifications, or to push a response of a
query in an async manner. It can be sent to the frontend if and whenever the
required data is available.


<a id="orgf1d5bac"></a>

## Generic undo/redo/revert.

Every mutation on a record adjust some metadata on the record that enables
undo/redo/revert for that record. This also includes any data joined to that
record, they will also get undone/redone/reverted.


<a id="orgc13b88e"></a>

# Backend parser


<a id="orgc30910c"></a>

## Table aliases

TODO-doc


<a id="org043f90c"></a>

## Virtual tables

TODO-doc


<a id="org38a0369"></a>

## Have backend return calculated data

There are three ways to do this:


<a id="org9c01444"></a>

### Calculate something over a (sub)query

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


<a id="orgcc756c5"></a>

### Define a read key in the backend

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


<a id="org4b20fa4"></a>

### Redirect a read to a custom-read

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


<a id="org61d2c55"></a>

# Testing


<a id="org7acbaec"></a>

## Backend testing

TODO-doc:


<a id="orgb78f0a5"></a>

## Frontend testing


<a id="org5deb589"></a>

### Install

nvm install
nvm use
npm install
npm install -g karma-cli


<a id="org92c6822"></a>

### Test runner

Standalone client-side om-next test-runner app to be used with the
alternative test macros that add and remove tests to the lists of tests. Several
ways to display diffs. Rerun test on click. Use snapshots for any test instead
of writing the required result into the test. Helpers to click and compare html
output for acceptance ui tests. Replay/rewind/step through (ui) tests by using
pause macro.


<a id="org73a4eef"></a>

### Snapshots

There are facilities to create a test by putting it together step by step and
instead inserting expected results take snapshots and use them instead. This is
particularly handy for testing states of the ui. It&rsquo;s also then possible to step
through the test in the test runner. If any intermediate snapshot fails the test
but (because we updated the code for example) is what we do expect we can update
the snapshot by clicking a button.


<a id="orgef3316e"></a>

## Run backend in frontend (for testing for example)

It is possible to run the whole backend in the frontend where the mysql database
is &rsquo;mocked&rsquo; in the frontend. This is ideal for writing integration tests
covering the whole stack

TODO-doc: add examples and working starter branch
NOTES:
Browser in memory sql options:
<https://github.com/kripken/sql.js>
<https://github.com/agershun/alasql/wiki/Getting%20started>


<a id="org04f6bb6"></a>

## Whole stack testing

By combining test runner, snapshot testing and running backend in frontend it&rsquo;s
possible to do whole stack testing.


<a id="org6313c2c"></a>

# Debugging


<a id="org2ae15c4"></a>

## Dev-cards

    Switch to dev cards page from app itself.
First install nvm (node version manager).


<a id="org0456b7d"></a>

## Frontend om inspector

Search, filter and drill into app and om state.


<a id="orgf4364a5"></a>

## It&rsquo;s possible to set some flags in local storage to get some output in console etc:

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


<a id="org2ceb406"></a>

### Trying queries

In the dev source folder there are namespaces to try out various queries:

1.  try-om-query

    You can call the backend parser with any om-next query. These are resolved
    against the database as defined in app.config and using database.config as
    defined for the whole app.
    
    There is a second version where you can build your own parser environment and
    your own parser with that again.

2.  Try sql query

    To try out any sql query. Make sure to define process-params, validate-sql-fn
    and process-result methods, and the equivalent sql fun in build-sql if you want
    it to be used in mock mode or tests.

3.  Try/test frontend parser.

    Frontend parser is a cljc file so you can eval this in a clojure repl. You can
    test here what the parser returns for queries for the nil and various remote
    targets, which is much harder to test/inspect if you have to use the ui to pass
    queries to the parser.


<a id="org8be04f8"></a>

## In boot-scripts there&rsquo;s tail.boot to inspect logstash output:

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


<a id="org8b859e9"></a>

# How to


<a id="org1a9f653"></a>

## Add npm modules

-   Add to package.json
-   Import package in index.js, set a global to imports
-   Create index.bundle.js by running npx webpack
-   Create externs file or add externs to foreign-libs.externs.ext.js
-   Edit resources/revolt.edn (and/or main.cljs.edn for figwheel):
-   Add any new externs file to the externs keys
-   Add entries for the exported packages to foreign-libs under the
-   foreign-libs/index.bundle.js entry:
-   -> The global created in index.js should be added to the global-exports subkey
    where the js global var name can be referred to by a clojure symbol ns
-   -> Add that symbol ns to to the provides key as a string.

\### Analyze size of webpack bundle

npx webpack &#x2013;config webpack.prod.js &#x2013;json > stats.json

Upload stat.json to <https://chrisbateman.github.io/webpack-visualizer/>

Or:

bin/analyze-webpackold-app-readme


<a id="org26ce28f"></a>

## Querying other sources than a mysql database


<a id="orgff77073"></a>

### Using more than one remote in the frontend

In `pagora.aum.frontend.reconciler.start` this function is defined:

    ```clojure
    (defn make-aum-remote [app-config]
      (fn [query response-cb]
        (let [chsk-send! (websocket/get-chsk-send!-fn app-config)]
          (chsk-send! [:aum/query query] (:websocket-timeout app-config 8000)
                      response-cb))))
    ```

You can add remotes like this:

    ```clojure
    {:my-remote (fn [app-config]
                  (fn [query response-cb]
                    ;;Call on the network with the query, call response-cb when response is received.
                    ))}
    ```

Add this map to app config under the :remotes key.

TODO-doc: add examples to starter app and document here


<a id="orgaf5cb11"></a>

### Returning data fetched from another source asynchronously

TODO-doc: add examples to starter app and document here
If a backend query can&rsquo;t be resolved and returned synchronously it&rsquo;s possible to
push the result to the frontend when it&rsquo;s available.


<a id="org9d25606"></a>

## Optimize frontend


<a id="org51eb643"></a>

### pathopt

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


<a id="org2dac1e5"></a>

## Start app with different ports and db:

DB<sub>NAME</sub>=my<sub>db</sub> SERVER<sub>PORT</sub>=9080 NREPL<sub>PORT</sub>=38401 bin/dev-backend


<a id="org51b3ba4"></a>

## throw catch exceptions

    ```clojure
    (try (throw (ex-info "ex-info msg string" {:type :my-exception :bla :foo}))
        (catch #?(:clj Throwable :cljs :default) e
        (let [msg (.getMessage e)
                data (ex-data e)]
            (info "Msg:" msg)
            (info "Data:" data))))
    ```


<a id="orgcda28d4"></a>

# Good to know


<a id="org504d54b"></a>

## Function and method signatures


<a id="orgc10c223"></a>

### read and mutate: [env key params] TODO-doc: update for aum

So, the read function you write:

Will receive three arguments:
An environment containing:
:parser:   The query parser
:state:    The application state (atom)
:query:    if the query had one E.g. {:people [:user/name]} has :query [:user/name]
A key whose form may vary based on the grammar form used (e.g. :user/name).
Parameters (which are nil if not supplied in the query)
Must return a value that has the shape implied by the grammar element being read.

The signature of a read function is:

(read [env dispatch-key params])

where the env contains the state of your application, a reference to your parser (so you can call it recursively, if you wish), a query root marker, an AST node describing the exact details of the element&rsquo;s meaning, a path, and anything else you want to put in there if you call the parser recursively.

The parse will create the output map.
(keys env) in mutation=>
(:query-root :path :pathopt :reconciler :ast :state :component :parser :logger :shared :target)
(keys env) in read =>
(:query-root :path :pathopt :ast :state :parser :logger :shared :target :query)


<a id="orgdaa4947"></a>

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


<a id="orgb0485c3"></a>

# Modules


<a id="orgd4f13b1"></a>

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


<a id="orgeee8992"></a>

## Use pages to organize your ui

TODO-aum:-
There are some basic fns for this. See app.pages for how to add a page.


<a id="org598e25c"></a>

## invalidation

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


<a id="org5744549"></a>

## Generic save records

When you have a page with records including their joins recursively you might
want to save the whole lot in one hit. Aum calculates the actual
modifications, and only sends what&rsquo;s changed to the backend. The backend then
will save these records in the right order, taking into account newly created
records and any dependencies on them and will if anything went wrong with
updating a record return this info per record. It garantuees to leave the db in
a consistent and validated state and returns enough information so the frontend
can correct any optimistically updates to its own app state and make sure it&rsquo;s
stays in sync with the backend.


<a id="orga11ed9a"></a>

## Database migration


<a id="org3e8b7eb"></a>

## Integrations


<a id="org0a3bb49"></a>

## Paging


<a id="orgac3d7d7"></a>

## Routing


<a id="orge920e05"></a>

## Internationalization


<a id="org00c3941"></a>

### There is a common.i18n.cljc namespace which provides the translate fn which

takes the current locale and a key.


<a id="orgbf118ab"></a>

### smarter translations

-   use params in translation keys, so interpolation
-   load translations zipped!!!???!!!!


<a id="org05b5e46"></a>

## Files download and upload


<a id="org50cec08"></a>

## Event store

Also see script in modules/events/experimental


<a id="org6317f39"></a>

## Calc active users


<a id="org83cfc57"></a>

## Icons

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


<a id="org23a8ac6"></a>

## Security


<a id="org98459fc"></a>

### login/logout

There&rsquo;s login/logout methods in app/security.cljc. Disabled in production.

Aum comes with login and logout fns for both front and backend. However in
production this is disabled and users are directed to the rails app.
The remember token as set by the rails app is used to authenticate the session
similar to how it&rsquo;s done in the rails app. One complication is that because how
sente/websockets work is that to renew the session and any attached remember
token the connection has to be renewed.


<a id="org72528e8"></a>

### bugsnag, authorization, login, logout etc

-   Load bugsnag api keys from gitignored .env file in update-html-string


<a id="orgc995b14"></a>

### Process-user and calc-role snippets

;; (defn superaccount? [db-conn account-id]
;;   (-> (q/get-cols-from-table db-conn {:cols [&ldquo;superaccount&rdquo; &ldquo;id&rdquo; &ldquo;name&rdquo;] :table &ldquo;accounts&rdquo;
;;                                       :where-clause [&ldquo;where id = ?&rdquo; account-id]})
;;       first
;;       :superaccount))

;; (defn calc-role
;;   &ldquo;Calculates role depending on account-id and any listing in admins table,&rdquo;
;;   [{:keys [db-conn config] :as env} {:keys [account-id ] :as user}]
;;   (when (some? user)
;;     (cond
;;       (= account-id (:pagora-account-id config)) &ldquo;super-admin&rdquo;
;;       :else (let [admin-account-ids (->> (q/get-cols-from-table db-conn {:cols [&ldquo;account<sub>id</sub>&rdquo;] :table &ldquo;admins&rdquo;
;;                                                                          :where-clause [&ldquo;where user<sub>id</sub> = ?&rdquo; (:id user)]})
;;                                        (map :account<sub>id</sub>))
;;                   account-admin? (cu/includes? admin-account-ids account-id)]
;;               (cond
;;                 account-admin? (if (superaccount? db-conn account-id) &ldquo;superaccount-admin&rdquo; &ldquo;account-admin&rdquo;)
;;                 :else &ldquo;user&rdquo;
;;                 )
;;               ))))

;; A much better option is a total separation of Users and Accounts. A user can
;; have several accounts (usually with a default one selected), and they can use
;; a single login to access each, and each account may have multiple users
;; associated with it.
;;So we need:
;;accounts<sub>users</sub> table

;; So account-id is not which account a user belongs to but which account the
;; user wants to access.

;; After that a user has a role within that account. Such as account-admin. If
;; the account is a super account (so administering more than just its own
;; account) then if the user has the account-admin role it might also have the
;; superaccount-admin

;;So we&rsquo;d need a accounts-users-roles table.

;; (defmethod process-user &ldquo;superaccount-admin&rdquo;
;;   [{:keys [db-conn] :as env} user]
;;   (let [role (calc-role env user)
;;         subaccount-ids (->> (q/get-cols-from-table db-conn {:cols [&ldquo;id&rdquo;] :table &ldquo;account&rdquo;
;;                                                             :where-clause [&ldquo;where account<sub>id</sub> = ?&rdquo; (:account-id user)]})
;;                          (mapv :id))
;;         ;;Can&rsquo;t be empty else sql query crashes (used in scope in database config)
;;         subaccount-ids (if (seq subaccount-ids) subaccount-ids [-1])] ;; but IN (-1) always results in false, same result.
;;     (assoc user
;;            :role role
;;            :subaccount-ids subaccount-ids)))

