(ns app.frontend.core
  (:require
   [app.frontend.config] ;;needs to be required
   [pagora.aum.frontend.core :as aum]
   [taoensso.timbre :as timbre]
   [app.frontend.root-component :refer [RootComponent]]
   ))

(let [aum-config (aum/init {:RootComponent RootComponent})]
  ;; (aum/go aum-config)
  )


