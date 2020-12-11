(ns app.frontend.core
  (:require
   [app.frontend.config] ;;needs to be loaded
   [app.frontend.mutations]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [pagora.aum.frontend.core :as aum]
   [taoensso.timbre :as timbre]

   [app.frontend.cells :refer [cells-dimensions]]
   [app.frontend.root-component :refer [RootComponent]]
   [app.frontend.compute-cells :refer [make-data]]))
;; ^:figwheel-no-load


(defonce start-app
  (let [app-state (om/tree->db RootComponent {:cells-data
                                              {:cells (into [] (make-data cells-dimensions))}} true)
        app-state  (merge app-state {:client/foo :from-app})
        aum-config (aum/init {:RootComponent RootComponent
                              :app-state app-state})]
    (aum/go aum-config)))
