(ns app.frontend.core
  (:require
   [app.frontend.config] ;;needs to be loaded
   [app.frontend.mutate]
   [app.frontend.read]
   [pagora.aum.om.next :as om]
   [pagora.aum.frontend.core :as aum]
   [taoensso.timbre :as timbre]

   [app.frontend.sevenguis.cells :refer [cells-dimensions]]
   [app.frontend.root-component :refer [RootComponent]]
   [app.frontend.cells-grammar :refer [Emptie]]))

(defn make-cells-data [{:keys [rows columns]}]
  (for [r (range rows) c (range columns)]
    {:r r :c c :value "" :formula Emptie :observers [] :content ""}))

(defonce start-app
  (let [app-state (om/tree->db RootComponent {:cells-data
                                              {:cells (into [] (make-cells-data cells-dimensions))}} true)
        aum-config (aum/init {:RootComponent RootComponent
                              :app-state app-state})]
    (timbre/info :#pp app-state)
    (aum/go aum-config)))
