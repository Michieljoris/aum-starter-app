(ns app.frontend.core
  (:require
   [app.frontend.config] ;;needs to be loaded
   [app.frontend.mutations]
   [pagora.aum.om.next :as om]
   [pagora.aum.frontend.core :as aum]
   [taoensso.timbre :as timbre]

   [app.frontend.sevenguis.cells :refer [cells-dimensions]]
   [app.frontend.root-component :refer [RootComponent]]
   [app.frontend.cells-grammar :refer [Emptie]]))
;; ^:figwheel-no-load

(defn make-cells-data [{:keys [rows columns]}]
  (for [r (range rows) c (range columns)]
    {:r r :c c :value "" :formula Emptie :observers [] :content ""}))

(defonce start-app
  (let [app-state (om/tree->db RootComponent {:cells-data
                                              {:cells (into [] (make-cells-data cells-dimensions))}} true)
        app-state  (merge app-state {:client/foo :from-app})
        aum-config (aum/init {:RootComponent RootComponent
                              :app-state app-state})]
    (aum/go aum-config)))
