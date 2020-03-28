(ns app.frontend.core
  (:require
   [app.frontend.config]
   [pagora.aum.frontend.core :as aum]
   [taoensso.timbre :as timbre]
   ))

(let [_ :foo]
  (let [aum-config (aum/init nil)]
    (aum/go aum-config))
  )

