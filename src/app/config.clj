(ns app.config
  (:require
   [pagora.aum.config :as aum]
   [taoensso.timbre :as timbre]))

(defmethod aum/config :common [_]
  {:multimethod-namespaces ['app.security]
   :pagora-account-id 1
   :timbre-log-level :info
   :app-path "app/"
   })

(defmethod aum/config :dev [_]
  {
   :db-password "irma"
   })

(defmethod aum/config :staging [_]
  {
   })

(defmethod aum/config :prod [_]
  {:db-pool true  ;whether to use c3p0 pool connection
   :db-pool-loglevel "INFO"
   :db-use-ssl false
   :min-pool-size 3
   :initial-pool-size 3
   :elasticsearch-log-result true
   :sql-log true ;print actual sql queries being made
   :query-log true
   :http-log false
   :gz-mime-types true
   :event-store-disabled false})

(defmethod aum/config :test [_]
  (def test-config
    {:db-name "test"                ;not used
     :om-next-test-db-name "om_next_test"
     :db-user "root"
     :db-password ""
     :db-pool false  ;whether to use c3p0 pool connection
     :db-print-spec true
     :sql-log false ;print actual sql queries being made
     :query-log false
     :http-log false
     :timbre-log-level :error
     :es-url "http://127.0.0.1:9200"}))
