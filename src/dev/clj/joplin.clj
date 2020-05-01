(ns joplin
  (:require
   [clojure.java.io :as io]
   [taoensso.timbre :as timbre]
   [clojure.java.jdbc :as jdbc]
   [joplin.jdbc.database]
   [joplin.repl :as repl]
   [joplin.core :as joplin]

   ))

(defn- load-config' [path]
  (-> (io/resource path)
      repl/load-config))

(def ^:dynamic *load-config* load-config')


(def config
  {
   :migrators {:sql-mig "resources/joplin/migrators/sql"}  ;; A path for a folder with migration files

   ;; :seeds {:sql-seed "seeds.sql/run"             ;; A clojure var (function) that applies the seed
   ;;         :es-seed "seeds.es/run"}

   :databases {:sql-dev {:type :jdbc, :url "jdbc:mysql://localhost:3306/foo?user=root&password=&zeroDateTimeBehavior=convertToNull&useSSL=false&characterEncoding=UTF-8"}
               ;; :es-prod {:type :es, :host "es-prod.local", :port "9300", :cluster "dev"}
               ;; :sql-prod {:type :jdbc, :url "jdbc:h2:file:prod"}
               }

   ;; We combine the definitions above into different environments
   :environments {:dev [{:db :sql-dev, :migrator :sql-mig, ;; :seed :sql-seed
                         }]
                  ;; :prod [{:db :sql-prod, :migrator :sql-mig}
                  ;;        {:db :es-prod}, ;; :seed :es-seed
                  ;;        ]
                  }
   })

(def db-name "aum_minimal2")
(def password "irma")
(def target {:db {:type :jdbc
                  ;; :migrations-table "joplin_migrations"
                  :url (str "jdbc:mysql://localhost:3306/" db-name "?user=root&password=" password "&serverTimezone=UTC&zeroDateTimeBehavior=convertToNull&useSSL=false&characterEncoding=UTF-8")}
             :migrator "joplin/migrators/sql"
             ;; :seed "name of a var in a namespace on the classpath"
             })

(comment
  (let [migration-name "foo"]
    (joplin/create-migration target migration-name)))

;; (repl/create config :dev :sql-dev "foo2")

(comment
  (joplin/migrate-db target)
  )

(comment
  (joplin/pending-migrations target)
  )

(comment
  (joplin/rollback-db target 1))

