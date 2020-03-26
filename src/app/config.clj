(ns app.config
  (:require
   [taoensso.timbre :as timbre]))

;; Config keys need to be assigned scalar values (so no maps or vectors) so we
;; can set them in env vars on the command line

;; Something like the following would run the jar (in the target directory)::
;; CLJ_ENV=prod DB_USER=test DB_PASSWORD=abc DB_URL="//localhost:3306/" DB_NAME=some_db QUERY_LOG=true  SQL_LOG=true HTTP_LOG=false SERVER_PORT=8080 SERVER_IP=0.0.0.0 java -jar prod.jar

(def common-config
  {:multimethod-namespaces ['app.security]
   :pagora-account-id 1
   :timbre-log-level :info
   })

(def prod-config
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
   :es-url "http://127.0.0.1:9200"})

(def dev-config {})

(def environments {:test (merge common-config test-config)
                   :dev (merge common-config dev-config)
                   :staging (merge common-config prod-config)
                   :prod (merge common-config prod-config)})

;;Merged with frontend config, so any config vars, or any other setting you want
;;the frontend to have access to add to the map returned by this macro.
;;NOTE: it is not possible to send env vars to the frontend!!!!!
;;you would have to set these env vars at compile time, for instance :clj-env
;; (defmacro public-config []
;;   (merge (select-keys config [:clj-env
;;                               :app-path
;;                               :email-regex
;;                               :locales
;;                               :max-image-dimensions
;;                               ])))
