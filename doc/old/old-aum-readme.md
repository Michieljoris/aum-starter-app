#+TITLE: # Aum next

- Server om-next parser

Use om-next queries to do crud on any mysql database, where the read can use one
or more joins from and to any table, constrained only by the (db-)configuration
of the parser.

- Client om-next reconciler and parser

Generic client side om-next parser and reconciler. Support for
routing/paging/undo-redo.

- Client test-runner

Standalone client-side om-next test-runner app to be used with the alternative
test macros that add and remove tests to the lists of tests. Several ways to
display diffs. Rerun test on click. Use snapshots for any test instead of
writing the required result into the test. Helpers to click and compare html
output for acceptance ui tests. Replay/rewind/step through (ui) tests by using
pause macro.

### Install

Run

    bin/install-git-deps

and then

    boot install-local

in this repo.

Add to your dependencies:

    [aum-next "0.1.0"]

Replace the version with the version in build.boot

Require:

    [bilby.parser :as bilby]

### Use

#### Server parser

    (def parser-env (bilby/parser-env {:parser-config {
                                        :normalize true}
                                    :db-conn some-db-conn}))

    (def parser (bilby/parser {:parser-env parser-env}))

    (def state (atom nil))  ;;will contain table if :normalize is true, and/or error data

    (def user {:id 1 :name "foo"}) ;;or nil.

    (bilby/parser {:state state :user user} om-next-query)

For more options to pass to bilby/parser-env see the bilby.parser namespace.

This parser will translate the om-next-query into a sql query and return the
result with the table data in the state atom if :normalize is true.

By default the user in env is checked to be truthy. If falsey {:value
:not-authorized} is returned by the bilby parser. To bypass this use the read
and mutate multimethods in bilby.parser-read and blby.parser.mutate namespaces:

Require

    [bilby.parser.read :refer [read]]
    [bilby.parser.mutate :refer [mutate]]

And define parser thusly:

    (def parser (bilby/parser {:parser-env parser-env
                               :read read
                               :mutate mutate}))

Or use your own mutate and/or read fns. The env will be populated with the keys
from the parser-env, as long as they are not overwritten by om-next env keys.
Optionally, the schema of the db will be under the :schema key

If using bilby read/mutate fns parser will still use the user map to check for
permissions and validations in db-config, and will still substitue namespaced
keywords in queries with values from the user map if possible.

By default the bilby parser will inspect the schema of the database given in the
db-conn and infer and check table and column names, and table joins. These can
also be explicitly described in db-config (TODO for table names and columns I think).

#### Client reconciler and parser

TODO: Add docs..

#### Test runner

TODO: Add docs..

### Result format

    {:value {...}
     :status :ok/:error
     :table-data {...}
     :original-table-data {...}}

- :status
can be :ok or :error. In the case of error one of the keys queried for
threw an error. Value of the key will be the error data.

- :table-data
will always have data as stored in the database, in other words it's
a subset of data in the database, this can be data as linked in to in a
(normalized) query result, data as queried for in save-record post-save

- :original-table-data
server table data after a failed mutation, potentially useful to repair frontend version of data

- :value
is the result of the query/mutation

In the case of a mutation :value will have this format:

    {mutation-symbol {:error {:stacktrace :not-returned
                            :context {...}
                            :message "..." }
                    :keys [..] }
                    :tempids [..]}

Any of tempids, keys and error is optional.

keys is a hint of the server for rerender, for affected table data.

In the case of error, for a mutation, the error message will the in the map for
the mutation symbol. For an error in reading a key, the error

### Develop

    boot watch-and-install

Besides being able to start a repl in this repo, if you start your (boot) project with the
-c (checkout) flag like this:

    boot -c aum-next:0.1.0 <some boot task(s)>

any edits in this repo's source code  will compiled, installed and checked out again
in your project.

#### Bumping and pushing new version to github

Bump +version+ at top of build.boot file to some x.x.x

Commit change.

git tag vx.x.x

git push origin : vx.x.x

## Test

    boot run-all-tests

or in repl

    (bilby.run-all-tests/eftest)

### TODOold-aum-readme
