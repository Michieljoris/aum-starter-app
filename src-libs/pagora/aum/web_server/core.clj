(ns pagora.aum.web-server.core
  (:require
   [integrant.core :as ig]
   [org.httpkit.server :as httpkit]
   [taoensso.timbre :as timbre]))

(defmethod ig/init-key ::httpkit-server [k {:keys [handler server-options]}]
  (let  [stop-server! (httpkit/run-server handler server-options)
         local-port (:local-port (meta stop-server!))
         uri (format "http://%s:%s/" (:ip server-options) local-port)]
    (timbre/info :#g "[INTEGRANT] creating" (name k))
    (timbre/info "Web server is running at " uri)
    {:local-port local-port
     :stop-server! stop-server!}))

(defmethod ig/halt-key! ::httpkit-server [k {:keys [stop-server!]}]
  (timbre/info :#g "[INTEGRANT] halting" (name k))
  (stop-server!))
