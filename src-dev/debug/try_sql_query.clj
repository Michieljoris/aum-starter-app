(ns debug.try-sql-query
  (:require
   [pagora.clj-utils.database.connection :as db-connection]
   [pagora.aum.database.process-params]
   [pagora.aum.database.process-result]
   [pagora.aum.database.validate-sql-fn]
   [pagora.aum.database.schema :as schema]
   [pagora.aum.database.query :refer [sql]]
   [clojure.java.jdbc :as jdbc]
   [pagora.aum.database.build-sql :as build-sql]
   [pagora.clj-utils.core :as cu]
   [app.database.config :refer [db-config]]
   [cuerdas.core :as str]
   [clojure.pprint :refer [pprint]]
   [taoensso.timbre :as timbre :refer [info]]))


;; Test sql fn
(comment
  (binding [pagora.aum.security/*schema-warnings* false]
    (let [db-conn (db-connection/make-db-connection {:url "//localhost:3306/"
                                                     :db-name "foo"
                                                     :print-spec true
                                                     :use-ssl false
                                                     :user "root"
                                                     :password ""})
          raw-schema (pagora.aum.database.schema/get-schema db-conn)
          schema     (pagora.aum.database.schema/make-condensed-schema raw-schema)
          env {:db-conn db-conn
               :schema (pagora.aum.security/secure-schema schema db-config)
               :user {:id 1 :group-id 1 :role "no-role"}
               :db-config db-config
               :sql {:hugsql-ns "database.queries"}
               :parser-config {:sql-log true
                               :event-store-disabled true}}]
      (time
       (clojure.java.jdbc/with-db-transaction [tx db-conn]
         (let [env (assoc env :db-conn tx)]
           (timbre/info (sql env :insert-record {:table :user
                                                 :skip-validate? false
                                                 :mods {:name "some user"}
                                                 }))

           (prn "is-rollback-only" (jdbc/db-is-rollback-only tx))
           ))))))
