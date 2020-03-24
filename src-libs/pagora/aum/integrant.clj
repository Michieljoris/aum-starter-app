(ns pagora.aum.integrant
  (:require
   [taoensso.timbre :as timbre]
   [integrant.core :as ig]))

(defn make-ig-config [{:keys [server] :as config}]
  {:pagora.aum.web-server.core/httpkit
   {:server-options server
    :handler (ig/ref :pagora.aum.web-server.handler/handler)}

   :pagora.aum.web-server.handler/handler
   {:config config
    :routes (ig/ref :pagora.aum.web-server.routes/routes)}

   :pagora.aum.web-server.routes/routes {:config config}

   :pagora.aum.database.connection/db-conn {:config config}

   }
  )
