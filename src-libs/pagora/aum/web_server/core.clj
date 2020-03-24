(ns pagora.aum.web-server.core
  (:require
   [integrant.core :as ig]
   [org.httpkit.server :as httpkit]
   [taoensso.timbre :as timbre]))

(defmethod ig/init-key ::httpkit [_ {:keys [handler server-options]}]
  (let  [stop-server! (httpkit/run-server handler server-options)
        local-port (:local-port (meta stop-server!))
         uri (format "http://%s:%s/" (:ip server-options) local-port)]
    (timbre/info "Web server is running at " uri)
    {:local-port local-port
     :stop-server! stop-server!}))

(defmethod ig/halt-key! ::httpkit [_ {:keys [stop-server!]}]
  (timbre/info "Stopping server!!!")
  (stop-server!))
