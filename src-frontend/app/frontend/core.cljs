(ns app.frontend.core
  (:require
   [app.frontend.config] ;;needs to be loaded
   [pagora.aum.frontend.core :as aum]
   [taoensso.timbre :as timbre]
   [app.frontend.root-component :refer [RootComponent]]))
;; ^:figwheel-no-load

(defonce start-app
  (let [aum-config (aum/init {:RootComponent RootComponent
                              :app-state {:client/foo :from-app}})]
    (aum/go aum-config)))
