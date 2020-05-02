(ns migrators.sql.20200330150018-create-accounts
  (:require
   [jdbc.core :as jdbc]
   [pagora.aum.modules.db-migration.joplin.core :refer [exec fetch]]
   [clojure.pprint :refer [pprint]]
   [stch.sql.ddl :refer :all]))  ;;DDL

;; https://stch-library.github.io/sql/

(defn up [db]
  (exec db (create
            (-> (table :accounts)
                (integer :id :unsigned :not-null :auto-increment)
                (varchar :name [255])
                (varchar :email [255])
                (primary-key :id)
                (index [:id]))
            (auto-inc 1)
            (engine :InnoDB)
            (collate :utf8-general-ci))))

(defn down [db]
  (exec db (drop-table :accounts)))
