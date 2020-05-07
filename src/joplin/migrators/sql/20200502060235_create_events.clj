(ns migrators.sql.20200502060235-create-events
(:require
   [pagora.aum.modules.db-migration.joplin.core :refer [exec fetch]]
   [stch.sql.ddl :refer :all]))

;; https://stch-library.github.io/sql/


(defn up [db]
  (exec db
        (create
         (-> (table :events)
             (integer :id :unsigned :not-null :auto-increment)
             (varchar :name [255])
             (varchar :entity-type [255])
             (integer :entity-id [10])
             (varchar :data [255])
             (integer :account-id [10] :unsigned :not-null)
             (integer :user-id [10] :unsigned :not-null)
             (integer :sequence-no [10])
             (integer :data-version [10])
             (varchar :created_at [32])
             (varchar :updated_at [32])
             (primary-key :id)
             (index [:id]))
         (auto-inc 1)
         (engine :InnoDB)
         (collate :utf8-general-ci))))

(defn down [db]
  (exec db (drop-table :events)))
