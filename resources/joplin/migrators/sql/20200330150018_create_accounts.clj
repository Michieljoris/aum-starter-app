(ns joplin.migrators.sql.20200330150018-create-accounts
  (:require
   [clojure.java.jdbc :as jdbc]
   [stch.sql.ddl :refer :all]))  ;;DDL

(defn up [db]
  (let [uri (:connection-uri db)
        sql (create
             (-> (table :accounts)
                 (integer :id :unsigned :not-null )
                 (varchar :name [255])
                 (varchar :email [255])
                 (primary-key :id)
                 (index [:id]))
             (auto-inc 1)
             (engine :InnoDB)
             (collate :utf8-general-ci))]
    (jdbc/db-do-commands uri [sql])))

(defn down [db]
  (let [sql (drop-table :accounts)
        uri (:connection-uri db)]
    (jdbc/db-do-commands uri [sql])))
