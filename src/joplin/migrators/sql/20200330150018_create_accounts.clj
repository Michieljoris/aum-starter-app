(ns migrators.sql.20200330150018-create-accounts
  (:require
   [jdbc.core :as jdbc]
   [cuerdas.core :as str]
   [clojure.pprint :refer [pprint]]
   [stch.sql.ddl :refer :all]))  ;;DDL

;; https://stch-library.github.io/sql/
;; https://github.com/stch-library/sql/blob/master/src/stch/sql/ddl.clj

(defn up [db]
  (let [uri (str/strip (:connection-uri db) "jdbc:")
        sql (create
             (-> (table :accounts)
                 (integer :id :unsigned :not-null :auto-increment)
                 (varchar :name [255])
                 (varchar :email [255])
                 (primary-key :id)
                 (index [:id]))
             (auto-inc 1)
             (engine :InnoDB)
             (collate :utf8-general-ci))]
    (with-open [conn (jdbc/connection uri)]
      (jdbc/execute uri [sql]))))

(defn down [db]
  (let [uri (str/strip (:connection-uri db) "jdbc:")
        sql (drop-table :accounts)]
    (with-open [conn (jdbc/connection uri)]
      (jdbc/execute uri [sql]))))
