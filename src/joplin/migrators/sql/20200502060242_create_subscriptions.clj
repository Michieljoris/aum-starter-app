(ns migrators.sql.20200502060242-create-subscriptions
(:require
   [pagora.aum.modules.db-migration.joplin.core :refer [exec fetch]]
   [stch.sql.ddl :refer :all]))

;; https://stch-library.github.io/sql/

(def tbl :subscriptions)

(defn up [db]
  (exec db (create
            (-> (table tbl)
                (integer :id :unsigned :not-null :auto-increment)
                (date :entry-at)
                (date :expired-at)
                (integer :account-id [10] :unsigned :not-null)
                (integer :user-id [10] :unsigned :not-null)
                (tiny-int :deleted)
                (tiny-int :invalidated)
                (varchar :created_at [32])
                (varchar :updated_at [32])
                (primary-key :id)
                (index [:id]))
            (auto-inc 1)
            (engine :InnoDB)
            (collate :utf8-general-ci))))

(defn down [db]
  (exec db (drop-table tbl)))
