(ns app.frontend.read
  (:require
   [taoensso.timbre :as timbre]
   [pagora.aum.frontend.parser.read :refer [read]])
  )


(defn make-auth-query [table query constraints]
    (let [{:keys [tables]} (-> constraints table)
          clauses (->> (select-keys constraints tables)
                       (reduce (fn [result [table {:keys [id]}]]
                                 (cond-> result
                                   (number? id) (conj [(keyword (str (name table) "-id")) := id])))
                               []))
          query {:auth [{table query}]}]

      (if (seq clauses)
        `(~query {:where [:and ~clauses]})
        query)))

(defn read-records [{:keys [db->tree state query context-data] :as env} table]
  ;; (timbre/info :#pp {:table table :context-data context-data})
  (let [data (->> (:auth context-data)
                  (mapv #(get-in @state (conj %1 table)))
                  distinct
                  (into []))]

    (db->tree env {:query query
                   :data data
                   :refs @state})))


(defmethod read [:value :account-records]
  [env key params]
  (timbre/info :#g "READ :account-records")
  (read-records env :account))

(defmethod read [:value :user-records]
  [env key params]
  (timbre/info :#g "READ :user-records")

  (read-records env :user))

(defmethod read [:value :role-records]
  [env key params]
  (timbre/info :#g "READ :role-records")
  (read-records env :role))

(defmethod read [:value :subscription-records]
  [{:keys [db->tree state target query parser ast context-data ast context-data] :as env} key params]
  [])




(defmethod read [:aum :account-records]
  [{:keys [db->tree state query parser ast context-data ast context-data] :as env} key params]
  (timbre/info :#g "REMOTE: account-records")
  (let [;; remote-query (db->tree env {:query query
        ;;                             :sparsify-query? true
        ;;                             :data context-data
        ;;                             :refs @state})
        auth-query (make-auth-query :account query (:client/auth-tables-state @state))]
    (timbre/info :#r "REMOTE ------------------------------")
    (timbre/info :#pp auth-query)

    ;; (timbre/info :#pp (merge (select-keys env [:context-data :query :target :default-remote])
    ;;                          {:key key :db->tree (db->tree env {:query query
    ;;                                                             :sparsify-query? true
    ;;                                                             :data context-data
    ;;                                                             :refs @state})
    ;;                           }))
    ;; (timbre/info :#r "------------------------------")

    ;; (when remote-query
    ;;   {(str key "-foo") auth-query})
    {:account-records [auth-query]}))

(defmethod read [:aum :user-records]
  [{:keys [db->tree state query parser ast context-data ast context-data] :as env} key params]
  (timbre/info :#g "REMOTE: user-records")
  (let [auth-query (make-auth-query :user query (:client/auth-tables-state @state))]
    (timbre/info :#pp {:auth-query auth-query})
    {:user-records [auth-query]}))

(defmethod read [:aum :role-records]
  [{:keys [db->tree state query parser ast context-data ast context-data] :as env} key params]
  (timbre/info :#g "REMOTE: role-records")
  (let [auth-query (make-auth-query :role query (:client/auth-tables-state @state))]
    {:role-records [auth-query]}))




  ;; (timbre/info :#r "VALUE ------------------------------")
  ;; (timbre/info :#pp (merge (select-keys env [:context-data :query :target :default-remote])
  ;;                          nil
  ;;                          ;; {:db->tree (db->tree env {:query query
  ;;                          ;;                           :data context-data
  ;;                          ;;                           :refs @state})
  ;;                          ;;  }
  ;;                          ))
