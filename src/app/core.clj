(ns app.core
  (:require
   [pagora.aum.core :as aum]
   [pagora.aum.config :refer [make-app-config]]
   [app.database.config :refer [db-config]]
   [taoensso.timbre :as timbre]
   ))

(defn start-app []
  (let [aum-config (aum/init {:app-config-ns 'app.config
                              :db-config db-config})]
    (aum/go aum-config)))

(defn -main [& args]
  (start-app))
