(ns migrators.sql.20200502051522-create-users
(:require
   [pagora.aum.modules.db-migration.joplin.core :refer [exec fetch]]
   [stch.sql.ddl :refer :all]))

;; https://stch-library.github.io/sql/

(defn up [db]
  (exec db (create
            (-> (table :users)
                (integer :id :unsigned :not-null :auto-increment)
                (tiny-int :active [1])
                (integer :account-id)
                (varchar :name [255])
                (varchar :email [255])
                (varchar :locale [255])
                (varchar :remember-token [255])
                (varchar :confirmation-token [255])
                (varchar :encryped-password [255])
                ;; (foreign-key :account-id '(accounts account-id) :on-delete-cascade)
                (primary-key :id)
                (index [:id]))
            (auto-inc 1)
            (engine :InnoDB)
            (collate :utf8-general-ci))))

(defn down [db]
  (exec db (drop-table :users)))

(create
            (-> (table :users)
                (integer :id :unsigned :not-null :auto-increment)
                (integer :account-id)
                (foreign-key :account-id '(accounts account-id) :on-delete-cascade)
                (primary-key :id)
                (index [:id]))
            (auto-inc 1)
            (engine :InnoDB)
            (collate :utf8-general-ci))
;; => "
