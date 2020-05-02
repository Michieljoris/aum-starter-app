(ns joplin
  (:require
   [clojure.java.io :as io]
   [jdbc.core :as jdbc]
   [taoensso.timbre :as timbre]
   ;; [clojure.java.jdbc :as jdbc]
    [stch.sql.ddl :refer :all]
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

(def target {:db {:type :jdbc
                  ;; :migrations-table "joplin_migrations"
                  :url (str "jdbc:" db-url)}
             :migrator "joplin/migrators/sql"
             ;; :seed "name of a var in a namespace on the classpath"
             })
(defn create-migrations-table [conn migrations-table]
  (let [sql (create
             (-> (table migrations-table)
                 (varchar :id [255])
                 (varchar :created_at [32]))
             (engine :InnoDB)
             (collate :utf8-general-ci))]
    (jdbc/execute conn [sql])))

(comment
  (defn recreate-db [user password db]
    (let [db-name "mysql"
          password password
          db-url (str "mysql://localhost:3306/" db-name "?user=" user
                      "&password=" password "&serverTimezone=UTC&zeroDateTimeBehavior=convertToNull&useSSL=false&characterEncoding=UTF-8")]
      (with-open [conn (jdbc/connection db-url)]
        (jdbc/execute conn [(str "DROP DATABASE IF EXISTS " db)])
        (jdbc/execute conn [(str "CREATE DATABASE IF NOT EXISTS " db)])))
    (let [db-name db
          password password
          db-url (str "mysql://localhost:3306/" db-name "?user=" user
                      "&password=" password "&serverTimezone=UTC&zeroDateTimeBehavior=convertToNull&useSSL=false&characterEncoding=UTF-8")]
      (with-open [conn (jdbc/connection db-url)]
        (create-migrations-table conn "joplin_migrations"))))
  (recreate-db "root" "irma" "aum_dev")

  )

(comment
  (with-open [conn (jdbc/connection db-url)]
    )
)

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
