(ns app.core
  (:require
   [pagora.aum.core :as aum]
   [app.database.config :refer [db-config]]
   [taoensso.timbre :as timbre]))

(def aum-params {:db-config db-config
                 :preprocess-config (fn [config] config);;TODO-aum
                 :app-config-ns 'app.config
                 :frontend-config-keys [:app-path :locales]})

(defn start-app []
  (let [aum-config (aum/init aum-params)]
    (aum/go aum-config)))

(defn -main [& args]
  (start-app))

(defn fahrenheit->celsius [fahrenheit]
  (* (- fahrenheit 32) 5/9))

(defn celsius->fahrenheit [celsius]
  (+ (* celsius 9/5) 32))
