(ns pagora.aum.core
  (:require
   [pagora.aum.integrant :refer [make-ig-config]]
   [taoensso.timbre :as timbre]
   [integrant.core :as ig]
   ))

(defonce config (atom nil))

(defn init [{some-config :config}]
  (reset! config some-config))

(defn -main [& args]
  (def system
    (ig/init (make-ig-config @config)))
  )

;; (ig/halt! system)
