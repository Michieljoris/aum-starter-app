(ns app.frontend.config
  (:require
   [pagora.aum.frontend.config :as aum]
   [taoensso.timbre :as timbre]
   ))

(defmethod aum/config :common [_]
  {
   :timbre-log-level :info
   })

(defmethod aum/config :dev [_]
  {
   :debug {:send true}
   })

(defmethod aum/config :staging [_]
  {
   })

(defmethod aum/config :prod [_]
  {
   })

(defmethod aum/config :test [_]
  {
   })
