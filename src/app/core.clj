(ns app.core
  (:require
   [pagora.aum.core :as aum]
   [app.config :refer [environments]]
   [app.database.config :refer [db-config]]
   [taoensso.timbre :as timbre]
   ))

(defn -main [& args]
  (let [aum-config (aum/init {:environments environments
                              :db-config db-config})]
    (aum/go aum-config)))
