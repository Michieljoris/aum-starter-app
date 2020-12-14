(ns app.frontend.read
 (:require
  [taoensso.timbre :as timbre]
  [pagora.aum.frontend.parser.read :refer [read]]))

(def mem (atom nil))

(defn read-cells-data [{:keys [db->tree state query context-data] :as env}]
  (let [refs (select-keys @state [:cells/by-rc])]
    (if (not= refs (:refs @mem))
      (let [result (db->tree env {:query query
                                  :refs refs
                                  :data context-data})]
        (swap! mem assoc :refs refs :result result)
        result)
      (:result @mem))))

(defmethod read [:value :cells]
  [env key params]
  (read-cells-data env))

;; Remote
(defmethod read [:aum :cells-data]
  [env key params]
  nil)
