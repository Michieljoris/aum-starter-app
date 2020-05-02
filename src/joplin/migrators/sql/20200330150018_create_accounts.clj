(ns migrators.sql.20200330150018-create-accounts
  (:require
   [pagora.aum.modules.db-migration.joplin.core :refer [exec fetch]]
   [stch.sql.ddl :refer :all]))  ;;DDL

;; https://stch-library.github.io/sql/

(defn up [db]
  (exec db (create
            (-> (table :accounts)
                (integer :id :unsigned :not-null :auto-increment)
                (varchar :name [255])
                ;; (varchar :created_at [32])
                ;; (varchar :updated_at [32])
                (primary-key :id)
                (index [:id]))
            (auto-inc 1)
            (engine :InnoDB)
            (collate :utf8-general-ci))))

(defn down [db]
  (exec db (drop-table :accounts)))
