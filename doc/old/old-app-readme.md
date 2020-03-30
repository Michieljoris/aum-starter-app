#+TITLE: # Bilby

## Install

Prerequisites:

- mysql
- redis
- nvm
- java 1.7 or greater

[Install clojure](https://clojure.org/guides/getting_started)

Make sure to checkout the 'templates' branch of chinchilla and do a rake db:migrate,
at least till the branch is merged.

In this repo do:

    nvm install
    nvm use

to make sure the proper version of node is used.

And then:

    bin/install

For the git/url deps to resolve ssh-agent needs to have a registered identity.
See <https://github.com/clojure/tools.gitlibs#ssh-authentication-for-private-repositories>

But in short:

    ssh-add

did it for me.

For development also do this:

    Make sure aum-next and dc-util repos are in the same parent dir as this repo.

    So if this repo is in ~/src/bilby for example, then you should also have
    ~/src/aum-next and ~/src/dc-util checked out to the most recent master.

    This is so that tools.deps can resolve the dependencies locally. If you
    want deps from github replace local-deps with git-deps in the bin/backend script

## Run the app

### Production

NOTE: the build task doesn't include deps in aliases into the uberjar. Need to
fix the build task, but for to create an uberjar uncomment the aum-next and
dc-util deps in deps.edn.

Build production jar at dist/app.jar with:

    bin/build

This'll fetch node modules and build the js bundle as well.

Set db user/password/url/db-name, server port and ip, and what logs you would
like by setting the various environment variables and run jar (in dist dir):

    CLJ_ENV=prod DB_USER=test DB_PASSWORD=abc DB_URL="//localhost:3306/" DB_NAME=chinchilla_development QUERY_LOG=true SQL_LOG=true HTTP_LOG=false SERVER_PORT=8081 SERVER_IP=0.0.0.0 NEW_RELIC_LICENSE_KEY="<some newrelic key>" java -javaagent:./newrelic-agent.jar -Dnewrelic.config.file=./newrelic.yml   -Dnewrelic.environment=production -jar dist/bilby.jar

or just

    bin/run

Or all in one cmd:

    bin/build-and-run

If tools.deps complains about dirs already existing when cloning repos delete ~/.gitlibs

Clearing classpath cache in ~/.clojure might also help.

See app at http://localhost:8090

Entry point of backend in production is at app.core. It requires web-server.core
which is at the root of the dependency tree and the main method that gets called
from the command line. The main method calls mount/start which starts up all the
stateful namespaces.

### Development

It's practical to run backend and frontend separately. It's rare you need
restart both, but being able to restarting one or the other sometimes is useful.

After bin/install:

    bin/backend

which will get you a clj repl in the terminal.

To set config settings, for example the db, do:

    DB_NAME=chinchilla bin/backend

See config.clj for possible settings and their defaults (replace
hyphens with underscores).

Connect your editor's repl to the nrepl server at port 5700.

In dev mode src-dev is on the classpath so src-dev/clj/user.clj gets loaded.
The sexpr (dev/start) is in that ns so it will be executed which will start the app.

In the repl start/stop/reset the app with (dev/start), (dev/stop), (dev/reset)

To compile the frontend, in another terminal do:

    bin/figwheel

which will get you a cljs repl in your terminal.

See app at localhost:8080

In the nrepl session in your editor run (user/cljs-repl) for a cljs repl

You might have to uncomment the connect-to-cljs-repl defn in
src/dev/cljs/cljs/user.cljs first.

Entry point of frontend is at app.core. Websocket is started here and this is
where om react tree gets mounted, after websocket first open event is received.

Entry point of backend in development is at dev.clj. It has the lifecycle methods. It requires
web-server.core which is at the root of the dependency tree.

NOTE: after building a prod jar, restart both backend and figwheel processes.
This is because the out dir is cleaned before building and the dev versions of both css and js
need to be built again. Or try modify a scss file and a cljs file to
kickstart recompile.


### Testing
First install nvm (node version manager).

Then

    nvm install
    nvm use
    npm install
    npm install -g karma-cli

TODO

## Debug production/staging

It's possible to set some flags in local storage to get some output in console
etc:

Set log level:

    :timbre-level :info

Click on DC logo and some debug buttons will show up:

    :debug-drawer true

Show what query is sent and what is returned:

    :send true

Show item id in lists:

    :display-item-id true

Show debug buttons in page bar:

    :debug-buttons true

In boot-scripts there's tail.boot to inspect logstash output:

    boot boot-scripts/tail.boot -h

Options:
  -h, --help        Print this help info.
  -f, --follow      follow
  -s, --start VAL   VAL sets start (line number or time (hh:mm) such as "11:10").
  -n, --length VAL  VAL sets number of lines or length of time such as "10h", "5m" "50s" If start is given then last so many lines or within last so much time.
  -t, --http-log    print http output lines
  -i, --timestamp   print timestamps
  -r, --regex VAL   VAL sets regex to filter lines.
  -l, --level VAL   VAL sets level to filter such as info or error.

## Translations

Keys given to common.i18n/translate can be a string or keyword. When capitalized
translation will be capitalized as well. These can, but don't need to be
prefixed with admin/. When looking up a key, key will be prefixed with admin/ if it's not
already. Keyword keys will be changed into strings (without leading keyword colon).

Examples:

    :foo lookup keyword in src code will require admin/foo key in translation database
    :admin/foo => admin/foo
    foo => admin/foo
    foo bar box => admin/foo bar box

The admin/ prefix allowes to fetch admin relevant translations only.

Set :mark-untranslated-keys to true in local storage and/or app.config.clj(s) to
show untranslated keys as the full key in brackets.


## Integrations

### Bugsnag
See integrations.clj and integrations.cljs.

Bugsnag is added to both front and backend. In app/config.clj both keys are set.
Bugsnag ring wrapper is added in webserver.handler.clj. See integrations.clj for
example of calling bugsnag-notify directly. See integrations.cljs for wrapper
fns to call bugsnag notify, breadcrumb and refresh in frontend.

To test bugsnag in development add valid keys to :dev config in app/config.clj.
If the key for the frontend is nil bugsnag script is not added to admin.html

### New relic

To test new relic in development:

Copy newrelic-agent.jar to repo dir, this uses jar version as specified in build.boot:

    boot copy-newrelic-jar

    NEW_RELIC_LICENSE_KEY="<some newrelic key>" bin/boot-with-jvm-options

See .boot-jvm-options for actual jvm options used. They include among other
options the -javaagent option. You should see some data popup in your newrelic.

To run new relic in production:

boot build task includes the copy-newrelic-jar task, so newrelic-agent.jar
should be in the projects root dir. Incantation below to run production jar
includes new relic api key env variable and jvm options to run new relic agent.
Logging is set to the cwd, as set in new relic config file at ./newrelic.yml.

If new relic license key env variable is not set new relic agent is not loaded.

### Logstash

Set logstash host, port, level and enabled in app.config.clj. Alternatively set
env variables logstash_host, logstash_port, logstash_level and logstash_enabled
before starting bilby. See dev.clj for trying logstash in development.

## Docs

aum-next has a draft of some docs.

## Git workflow
A la http://nvie.com/posts/a-successful-git-branching-model/

Basically, master branch is deploy ready and is tagged with latest version.

release_master branch incorporates new features as they are developed.

Any feature branches get merged into release_master as they are ready.

When there's lots of feature commits to release_master:

    Branch off a release-x.x (no patch version) branch from release_master.
    Prepare this branch to to be merged into master once it's stable and
    properly tested.

    Merge release-x.x branch into master (tag commit with patch version 0), and
    also back into release_master once ready, and delete it.

Otherwise release_master is the branch getting ready to be released

Any hotfix branches (to fix master and branched off it) are like release
branches. They should bump patch version and should get merged back into master and into release-x.x branch if
there is one, otherwise with release_master.

Get version by adding build.json to url of app, so for instance http://localhost:8080/admin/build.json

## HOW TO

### Modify url path of app
- End path with slash
- Modify path var at top of build.boot
- Modify path var at top of app/config.clj
- Move content of resources/<path> and src/cljs/<path> to the new path.
- Edit resources/<new path>/admin.html and set new path for css and js files

### Cursive

### Tips
See for graph of dependencies ns-hierarchy.png. Produced with medusa. Might be outdated.

Enable/disable various debugging settings in cljs/app/config.cljs. Very handy to
work out what om-next is actually doing.

### Mobile debugging:

* Set vorlon-script to true in config.clj or set env var.
* Install vorlon: http://vorlonjs.com/
* Run vorlon on commandline
* Open mobile device at your lan interface:port
* Open vorlorn dashboard at localhost:1337

### Add npm modules

* Add to package.json
* Import package in index.js, set a global to imports
* Create index.bundle.js by running npx webpack
* Create externs file or add externs to foreign-libs.externs.ext.js
* Edit resources/revolt.edn (and/or main.cljs.edn for figwheel):
* Add any new externs file to the externs keys
* Add entries for the exported packages to foreign-libs under the
  foreign-libs/index.bundle.js entry:
* -> The global created in index.js should be added to the global-exports subkey
  where the js global var name can be referred to by a clojure symbol ns
* -> Add that symbol ns to to the provides key as a string.

### Analyze size of webpack bundle

    npx webpack --config webpack.prod.js --json > stats.json

Upload stat.json to https://chrisbateman.github.io/webpack-visualizer/

Or:

    bin/analyze-webpackold-app-readme
