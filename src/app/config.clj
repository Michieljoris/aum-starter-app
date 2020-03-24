(ns app.config
  (:require
   [pagora.aum.config :as aum]
   [app.database.config :refer [db-config]]
   [taoensso.timbre :as timbre]
   ))

;; Config keys need to be assigned scalar values (so no maps or vectors) so we
;; can set them in env vars on the command line

;; Something like the following would run the jar (in the target directory)::
;; CLJ_ENV=prod DB_USER=test DB_PASSWORD=abc DB_URL="//localhost:3306/" DB_NAME=some_db QUERY_LOG=true  SQL_LOG=true HTTP_LOG=false SERVER_PORT=8080 SERVER_IP=0.0.0.0 java -jar prod.jar

(def prod-config
  {:clj-env :prod

   ;; Web server
   :server-port nil
   :server-ip nil

   ;; Mysql database
   :db-url nil
   :db-name nil
   :om-next-test-db-name "om_next_test"
   :db-user nil
   :db-password nil
   :db-pool true  ;whether to use c3p0 pool connection
   :db-pool-loglevel "INFO"
   :db-use-ssl false
   :min-pool-size 3
   :initial-pool-size 3
   :db-print-spec true

   :elasticsearch-url nil
   :elasticsearch-log-result true

   ;; Logging
   :sql-log true ;print actual sql queries being made
   :query-log true
   :http-log false
   :timbre-log-level :info

   :logstash-host "0.0.0.0"
   :logstash-port 12345
   :logstash-level :info
   :logstash-enabled true

   :gz-mime-types true

   :obs-access-key "foo"
   :obs-secret-key "bar"
   :obs-bucket "??"
   :obs-region "eu-de"
   :obs-service "s3"

   ;;NOTE: disabled for this deploy only so that initial value events can be recorded by script.
   :event-store-disabled true
   }
  )

(def test-config
  {:clj-env :dev

   ;; Web server
   :server-port "8080"
   :server-ip "0.0.0.0"

   ;; Mysql database
   :db-url "//localhost:3306/"
   :db-name "foo"                ;not used
   :om-next-test-db-name "om_next_test"
   :db-user "root"
   :db-password ""
   :db-pool false  ;whether to use c3p0 pool connection
   :db-print-spec true

   ;; Logging
   :sql-log false ;print actual sql queries being made
   :query-log false
   :http-log false

   :timbre-log-level :error

   :es-url "http://127.0.0.1:9200"
   })

(def config (aum/make-config {:environments {:dev {}
                                             :prod prod-config
                                             :staging prod-config
                                             :test test-config}
                              :db-config db-config}))

;;Merged with frontend config, so any config vars, or any other setting you want
;;the frontend to have access to add to the map returned by this macro.
;;NOTE: it is not possible to send env vars to the frontend!!!!!
;;you would have to set these env vars at compile time, for instance :clj-env
(defmacro public-config []
  (merge (select-keys config [:clj-env
                              :app-path
                              :email-regex
                              :locales
                              :max-image-dimensions
                              ])))

