(ns user
  (:require
   [app.config :refer [config]]
   [pagora.aum.dev :as dev]
   [integrant.repl :refer [clear go halt init prep reset reset-all]
    ]
   ;; [app.backend.core]
   ))

(dev/init config)
(go)
;; (reset)

;; (go)
;; (halt)
