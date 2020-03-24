(ns pagora.aum.backend.integrant
  (:require
   [integrant.core :as ig]))

(defn make-ig-config [{:keys [server] :as config}]
  {:pagora.aum.backend.web-server.core/httpkit
   {:server-options server
    :handler (ig/ref :pagora.aum.backend.web-server.handler/handler)}

   :pagora.aum.backend.web-server.handler/handler
   {:config config
    :routes (ig/ref :pagora.aum.backend.web-server.routes/routes)}

   :pagora.aum.backend.web-server.routes/routes {:config config}
   ;; :routes/default {:config config}
   }
  )
