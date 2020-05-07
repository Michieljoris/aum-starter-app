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
                                    (apply conj s (keys row))) #{})
                          (reduce (fn [m kw]
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
                 (values (pad-rows [{:id 1 :name "Account 1"}
                                    {:id 2 :name "Account 2"}
                                    {:id 3 :name "Account 3"}
                                    ])))
   :users (-> (insert-into :users)
              (values (->> [{:name "Foo" :email "foo@b.com"}
                            {:name "Bar" :email "bar@b.com"}
                            {:name "Baz" :email "baz@b.com"}
                            ]
                           pad-rows number-rows)))
   :roles (-> (insert-into :roles)
              (values (pad-rows [{:name "master-admin"}
                                 {:name "account-admin"}
                                 {:name "master-account-admin"}
                                 {:name "user"}])))
   :subscriptions (-> (insert-into :subscriptions)
                      (values (->> [{:account-id 1 :user-id 1 :entry-at "2000-10-10"}]
                                   pad-rows number-rows)))
   :auth (-> (insert-into :auth)
             (values (->> [{:account-id 1 :user-id 1 :role-id 1}
                           {:account-id 2 :user-id 1 :role-id 2}
                           {:account-id 1 :user-id 2 :role-id 1}
                           {:account-id 2 :user-id 3 :role-id 3}
                           {:account-id 2 :user-id 3 :role-id 4}
                           ]
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
