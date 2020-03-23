(ns try-sql-query
  (:require
   [digicheck.database.connection :as db-connection]
   [bilby.database.process-params]
   [bilby.database.process-result]
   [bilby.database.schema :as schema]
   ;; [app.environment :as env]
   [bilby.database.clauses :as db-clauses]
   [bilby.database.query :refer [sql]]
   [clojure.java.jdbc :as jdbc]
   [bilby.database.build-sql :as build-sql]
   [digicheck.common.util :as du]
   [database.config :refer [db-config]]
   [cuerdas.core :as str]
   [clojure.pprint :refer [pprint]]
   [taoensso.timbre :as timbre :refer [info]]))


;; Test sql fn
(comment
  (binding [bilby.security/*schema-warnings* false]
    (let [db-conn (db-connection/make-db-connection {:url "//localhost:3306/"
                                                     :db-name "chin_dev"
                                                     ;; :db-name "chinchilla_development"
                                                     :print-spec true
                                                     :use-ssl false
                                                     :user "root"
                                                     :password ""})
          raw-schema (bilby.database.schema/get-schema db-conn)
          schema     (bilby.database.schema/make-condensed-schema raw-schema)
          env {:db-conn db-conn
               :schema (bilby.security/secure-schema schema db-config)
               :user {:id 1 :group-id 10 :subgroup-ids [2 3] :role "super-admin"}
               :db-config db-config ;; {:event-store {:table-name :event-store
               ;;      :delete {:scope [:name := 100]}
               ;;      :update {:scope [:or [[:group-id := :u/group-id]
               ;;                            [:group-id :in :u/subgroup-ids]]]
               ;;               :whitelist [:id :name]}
               ;;      }}
               :sql {:hugsql-ns "database.queries"}
               :parser-config {:sql-log true
                               :event-store-disabled true}}
          ]

      (time
       (clojure.java.jdbc/with-db-transaction [tx db-conn]
         (let [env (assoc env :db-conn tx)]
           ;; (sql (assoc env :db-conn tx) :count {:table :user
           ;;                                      :where [:id :< 10]})
           (prn "is-rollback-only" (jdbc/db-is-rollback-only tx))

           ;; (timbre/info (sql env :insert-rows {:table :qbucket
           ;;                                     :skip-validate? true
           ;;                                     :rows [{:name "foo"} {:name "bar"}]}))

           ;; (timbre/info (sql env :insert-record {:table :qbucket
           ;;                                       :skip-validate? true
           ;;                                       :mods {:name 1}
           ;;                                       }))
           ;; (sql env :insert-rows {:table :template-user
           ;;                        :rows [{:template-id 2 :user-id 1} {:template-id 1 :user-id 1}]})
           ;; (sql env :delete-record {:table :template-user
           ;;                          :mods {:foo :bar}
           ;;                          :where [:and  [[:template-id := 1] [:user-id :in (set [1 2 3])]]]})
           ;; (sql env :insert-rows {:table :template-user
           ;;                        :rows [{:template-id 2 :user-id 1} {:template-id 1 :user-id 1}]})
           ;; (sql env :delete-record {:table :template-user
           ;;                          :mods {:foo :bar}
           ;;                          :where [:and  [[:template-id := 1] [:user-id :in (set [1 2 3])]]]})
           (sql env :template+user {:where-clause
                                    (db-clauses/make-where-clause {
                                                                   :scope [:c.group-id := 11]
                                                                   ;; :cond [:id := 2]
                                                                   :where [:a.name := "Test vraag types"]
                                                                   ;; :props props
                                                                   :derive-cols? true
                                                                   })})
           )
           ;; (timbre/info (sql env :ids {:skip-validate? true}))

           ;; (doseq [id (range (- 6255 5468))]
           ;;   ;; (sql env :update-record  {:table :qbucket
           ;;   ;;                           :skip-validate? true
           ;;   ;;                           :no-event? true
           ;;   ;;                           :id (+ 5468 id)
           ;;   ;;                           :mods {:name "FOOOO2"}
           ;;   ;;                           :current-record {:a 1}
           ;;   ;;                           })
           ;;   )
           )

         ;; (throw (Exception. "sql/test exception"))
         ))




      ;; (sql env :update-record  {:table :qbucket
      ;;                           ;; :skip-validate? true
      ;;                           :id 2305
      ;;                           :mods {:name "FOOOO Stookinstallatie - Gasmotor - WKK - Gas - ≥ 2,5 MW - Emissienorm - Rapport"}
      ;;                           :current-record {:a 1}
      ;;                           })
      )))




;; (let [clause
;;       (db-clauses/make-where-clause {:table-name "templates_users"  :derive-cols? true :where [:and  [[:template-id := 1] [:user-id := 1]]]})]
;;   (into [(subs (first clause) 6)] (rest clause))
;;  clause
;;   )
;; => ["(`templates_users`.`template_id` = ? AND `templates_users`.`user_id` = ?)" 1 1]
;; => ["where (`templates_users`.`template_id` = ? AND `templates_users`.`user_id` = ?)" 1 1]

;; (let [clause
;;       (db-clauses/make-where-clause {:table-name "t1" :cols [:t1.group_id :t2.translation_id:a.b] :where [:and  [[:t1.group_id := 2][:t1.group_id := 1]]]})]
;;   (into [(subs (first clause) 6)] (rest clause))
;;   )
;; (db-clauses/make-where-clause {:table-name "t1a" :cols [:t1.group_id :t2.translation_id:a.b] :where [:and  [[:t1.group_id := 2][:a.b := 1]]]})
;; ;; => ["where (`t1a`.`a` = ? AND `t1a`.`a` = ?)" 2 1]

;;  where t1.group_id is null and ((t2.translation_id = t1.id and t2.group_id = :group-id) or (t2.translation_id is null and t2.id = t1.id))

(db-clauses/make-where-clause {
                               :where [:template.id := 1]
                               :cond [:user.group-id := 10]
                               :scope [:template-user.id := 1]
                               ;; :props props
                               :derive-cols? true
                               })
