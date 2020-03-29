(ns app.frontend.core
  (:require
   [app.frontend.config] ;;needs to be required
   [pagora.aum.frontend.core :as aum]
   [taoensso.timbre :as timbre]
   [app.frontend.root-component :refer [RootComponent]]
   ))
;; ^:figwheel-no-load

(defonce start
  (let [aum-config (aum/init {:RootComponent RootComponent})]
    (aum/go aum-config)))
