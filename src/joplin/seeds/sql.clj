(ns seeds.sql
  (:require
   ;; [clojure.java.jdbc :as jdbc]
   [taoensso.timbre :as timbre]
   [jdbc.core :as jdbc]
   [stch.sql :refer [insert-into values]]
   [stch.sql.format :as sql]
   [cuerdas.core :as str]
   [stch.sql.ddl :refer :all]
   [clojure.pprint :refer [pprint]]))

;; https://stch-library.github.io/sql/
;; https://github.com/stch-library/sql/blob/master/src/stch/sql/ddl.clj

(do
  (defn pad-rows [rows]
    (let [padded-row (->> rows
                          (reduce (fn [s row]
                                    (println s)
                                    (apply conj s (keys row))) #{})
                          (reduce (fn [m kw]
                                    (println kw)
                                    (assoc m kw nil)) {}))]
      (->> rows (map #(merge padded-row %)))))


  ;; (pad-rows [{:name "Foo" :email "foo@b.com" :account-id 10}
  ;;            {:name "Bar" :email "bar@b.com"}])
  )

(defn number-rows [rows]
  (->> rows (map-indexed #(assoc %2 :id (inc %1)))))

  ;; (number-rows [{:name "Foo" :email "foo@b.com" :account-id 10}
             ;; {:name "Bar" :email "bar@b.com"}])

(def seeds
  {:accounts (-> (insert-into :accounts)
                 (values (pad-rows [{:id 1 :name "Some account"}])))
   :users (-> (insert-into :users)
              (values (->> [{:name "Foo" :email "foo@b.com"}
                            {:name "Bar" :email "bar@b.com"}]
                           pad-rows number-rows)))
   :roles (-> (insert-into :roles)
              (values (pad-rows [{:name "master-admin"}
                                 {:name "account-admin"}
                                 {:name "master-account-admin"}
                                 {:name "user"}])))
   :subscriptions (-> (insert-into :subscriptions)
                      (values (->> [{:account-id 1 :user-id 1 :entry-at "2000-10-10"}]
                                   pad-rows number-rows)))
   :accounts-users-roles (-> (insert-into :accounts-users-roles)
                             (values (->> [{:account-id 1 :user-id 1 :role-id 1}]
                                          pad-rows number-rows)))
   })
(defn run [target & seed-kws]
  (let [{{:keys [url]} :db} target
        url (str/strip url "jdbc:")
        conn (jdbc/connection url)]
    (with-open [conn (jdbc/connection url)]
      (doseq [seed-kw seed-kws]
        (let [seed (get seeds (keyword seed-kw))
              sql-vec (sql/format seed)]
          (timbre/info sql-vec)
          (jdbc/execute conn sql-vec))))))
