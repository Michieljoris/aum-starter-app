(ns app.backend.core
  (:require [pagora.aum.backend.core :as aum]
            [app.config :refer [config]]))

(defn -main [& args]
  (aum/init {:config config}))
