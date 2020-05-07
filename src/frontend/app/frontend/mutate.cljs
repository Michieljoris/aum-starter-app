(ns app.frontend.mutate
  (:require
   [pagora.aum.om.next :as om]
   [taoensso.timbre :as timbre]
   [pagora.aum.frontend.parser.mutate :refer [mutate]]))

(defmethod mutate 'aum/set-selected-auth-id
  [{:keys [state] :as env} _ {:keys [table id]}]
  {:action (fn []
             (let [om-path (-> env :component om/props meta :om-path)]
               ;; (timbre/info :#pp {:om-path om-path} )
               ;; (swap! state dissoc :user-records  :role-records nil)
               )
             (swap! state assoc-in [:client/auth-tables-state table :id] id))})

(defmethod mutate 'aum/set-table-constraints
  [{:keys [state] :as env} _ {:keys [table selected-table checked]}]
  {:action (fn []
             (swap! state update-in [:client/auth-tables-state table :tables]
                    (fn [s] (let [action (if checked conj disj)]
                              (action (or s #{}) selected-table))))
             (timbre/info :#pp (:client/auth-tables-state @state))

             )})
