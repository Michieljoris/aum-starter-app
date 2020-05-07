(ns migrators.sql.20200507022418-auth
(:require
   [pagora.aum.modules.db-migration.joplin.core :refer [exec fetch]]
   [stch.sql.ddl :refer :all]))

;; https://stch-library.github.io/sql/

(def table-kw :auth)

(defn up [db]
  (exec db (create
            (-> (table table-kw)
                (integer :id :unsigned :not-null :auto-increment)
                (integer :account-id [10] :unsigned :not-null)
                (integer :user-id [10] :unsigned :not-null)
                (integer :role-id [10] :unsigned)
                ;; (varchar :created_at [32])
                ;; (varchar :updated_at [32])
                (primary-key :id)
                (index [:id])
                )
            (auto-inc 1)
            (engine :InnoDB)
            (collate :utf8-general-ci))))

(defn down [db]
  (exec db (drop-table table-kw)))
