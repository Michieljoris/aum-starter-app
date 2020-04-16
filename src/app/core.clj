(ns app.core
  (:require
   [pagora.aum.core :as aum]
   [pagora.aum.config :refer [make-app-config]]
   [app.database.config :refer [db-config]]
   [taoensso.timbre :as timbre]))

(def aum-params {:db-config db-config
                 :app-config-ns 'app.config
                 :frontend-config-keys [:app-path :locales]})

(defn start-app []
  (let [aum-config (aum/init aum-params)]
    (aum/go aum-config)))

(defn -main [& args]
  (start-app))
