(ns debug.try-om-query
  (:require
   [pagora.aum.dev.core :as dev]
   [pagora.clj-utils.core :as du]
   [pagora.aum.database.inspect :as db-inspect]
   [cuerdas.core :as str]
   [pagora.aum.database.schema :as schema]
   [pagora.aum.config :refer [parser-config]]
   [taoensso.timbre :as timbre :refer [error info warn]]
   [jansi-clj.core :as jansi]
   [pagora.aum.database.jdbc-defaults :as jdbc-defaults]
   [pagora.aum.security :as security]
   [clojure.pprint :refer [pprint]]
   [pagora.aum.om.util :as om-util]
   [pagora.aum.parser.core :refer [parser]]
   [clojure.inspector :as inspect]
   ;; [inspector-jay.core :as jay]

   ;; [net.cgrand.packed-printer :refer [pprint]]

   [pagora.aum.database.schema :refer [get-schema]]

   ;; [dc-admin.backend.app.config :refer [config]]
   [app.database.config :refer [db-config]]
   ;; [database.connection :refer [db-conn]]
   [pagora.aum.parser.core :as pagora.aum]

   [pagora.aum.database.query :as query]
   ;;Do not remove. Loads pagora.aum multimethods for validating sql fn and processing
   ;;params/result
   [pagora.aum.database.process-params]
   [pagora.aum.database.validate-sql-fun]
   [pagora.aum.database.process-result]

   ;;Do not remove. This loads methods to the pagora.aum.parser.mutate/mutate and
   ;;pagora.aum-parser.read/read multimethods
   [pagora.aum.parser.read]
   [pagora.aum.parser.mutate]
   ;; [database.query-hooks]
   [fipp.edn :refer (pprint) :rename {pprint fipp}]
   [clojure.set :as set]
   [pagora.aum.database.inspect :as db-inspect]
   [pagora.clj-utils.database.connection :refer [make-db-connection]]
   )
  )



;; Try out the parser as actually used for the app:
(comment
  (let [
        query [{:user [:id :name]}]
        ;; query '[(admin/save-user
        ;;          {:table :user,
        ;;           ;; :id 1,
        ;;           :query nil,
        ;;           :mods {:name "user 2"},
        ;;           :_post-remote {:param-keys [:id :table :query]}})]

        do-query (fn [query]
                   (println "++++++++++++++++++++++++++++++++++++++++++++++++++++++")
                   (let [db-conn (make-db-connection {:url "//localhost:3306/"
                                                      :db-name "foo"
                                                      :print-spec true
                                                      :use-ssl false
                                                      :user "root"
                                                      :password ""})
                         raw-schema (schema/get-schema db-conn)
                         schema     (schema/make-condensed-schema raw-schema)
                         ;; schema (security/secure-schema  schema db-config)
                         ;; _ (pprint (get schema "users"))
                         state      (atom {:status :ok})
                         user {:id 1990 :some-user "afoobar" :role "super-admin" :group-id 62 :subgroup-ids [154]}
                         env        {:parser-config (merge parser-config {;; :allow-root true
                                                                          :print-exceptions true
                                                                          :normalize true})
                                     :db-conn       db-conn
                                     :db-config     db-config
                                     :schema        (security/secure-schema schema db-config)
                                     :raw-schema    raw-schema
                                     :state         state
                                     :user          user}
                         parser (:pagora.aum.parser.core/parser (dev/ig-system))
                         result    (parser env query)]
                     (do
                       (info "State:")
                       (pprint @state)
                       (println "----------------------------------------")
                       (info "Result:")
                       (fipp result))))]
    (try (do-query query)
      (catch Exception e
        (throw e)
        (info (jansi/red "DEBUGGING STATEMENTS ARE STILL ENABLED IN READ.CLJ!!!!"))))))


