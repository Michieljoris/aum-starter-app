(ns pagora.aum.integrant
  (:require
   [taoensso.timbre :as timbre]
   [integrant.core :as ig]))

(defn make-ig-config [{:keys [server] :as config}]
  {
   :pagora.aum.web-server.routes/routes
   {:config config}

   :pagora.aum.web-server.handler/handler
   {:config config
    :routes (ig/ref :pagora.aum.web-server.routes/routes)}

   :pagora.aum.web-server.core/httpkit
   {:server-options server
    :handler (ig/ref :pagora.aum.web-server.handler/handler)}

   :pagora.aum.database.connection/db-conn
   {:config config}

   :pagora.aum.parser.core/parser-env
   {:config config
    :db-conn (ig/ref :pagora.aum.database.connection/db-conn)
    }
  
   :pagora.aum.parser.core/parser
   {:config config
    :parser-env (ig/ref :pagora.aum.parser.core/parser-env)}}

  )

(def namespaces-with-defmethods
  ['pagora.aum.database.process-params
   'pagora.aum.database.validate-sql-fun
   'pagora.aum.database.process-result
   ;; 'pagora.aum.parser.read
   ;; 'pagora.aum.parser.mutate
   ;; 'database.query-hooks
   ])

(defn- try-require [sym]
  (try (do (require sym) sym)
       (catch java.io.FileNotFoundException _)))

(defn load-namespaces [symbols]
  (doall (->> symbols (map try-require))))

(load-namespaces namespaces-with-defmethods)
