(ns seeds.sql
  (:require
   ;; [clojure.java.jdbc :as jdbc]
   [jdbc.core :as jdbc]
   [stch.sql :refer [insert-into values]]
   [stch.sql.format :as sql]
   [cuerdas.core :as str]
   [stch.sql.ddl :refer :all]
   [clojure.pprint :refer [pprint]]))

;; https://stch-library.github.io/sql/
;; https://github.com/stch-library/sql/blob/master/src/stch/sql/ddl.clj

(def seeds
  {:seed1
   (-> (insert-into :accounts)
       (values [{:name "Some account"}]))})

(defn run [target & [seed]]
  (let [seed (or seed :seed1)
        {{:keys [url]} :db} target
        url (str/strip url "jdbc:")
        conn (jdbc/connection url)
        sql-vec (sql/format (get seeds (keyword seed)))]
    ;; (println sql-vec)
    (with-open [conn (jdbc/connection url)]
      (jdbc/execute conn sql-vec))))
