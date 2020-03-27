(ns pagora.aum.core
  (:require
   [pagora.aum.config :refer [make-app-config]]
   [taoensso.timbre :as timbre]
   [integrant.core :as ig]
   [pagora.clj-utils.timbre :refer [middleware]]
   ))

(defn make-ig-system-config [{:keys [server] :as config}]
  {
   :pagora.aum.web-server.routes/routes
   {:config config}

   :pagora.aum.web-server.handler/handler
   {:config config
    :routes (ig/ref :pagora.aum.web-server.routes/routes)}

   :pagora.aum.web-server.core/httpkit-server
   {:server-options server
    :config config
    :handler (ig/ref :pagora.aum.web-server.handler/handler)}



   :pagora.aum.database.connection/db-conn
   {:config config}

   :pagora.aum.parser.core/parser-env
   {:config config
    :db-conn (ig/ref :pagora.aum.database.connection/db-conn)
    }
  
   :pagora.aum.parser.core/parser
   {:config config
    :parser-env (ig/ref :pagora.aum.parser.core/parser-env)}

   :pagora.aum.websockets.core/websocket-listener
   {:config config
    :parser (ig/ref :pagora.aum.parser.core/parser)
    :parser-env (ig/ref :pagora.aum.parser.core/parser-env)}
   }
  )

(def aum-multimethod-namespaces
  ['pagora.aum.database.validate-sql-fun
   'pagora.aum.database.process-params
   'pagora.aum.database.process-result
   ;; 'database.query-hooks
   ])

(defn- try-require [sym]
  (try (do (require sym) sym)
       (catch java.io.FileNotFoundException _)))

(defn load-namespaces [symbols]
  (doall (->> symbols (map try-require))))

(defn init
  [{:keys [environments db-config]}]
  (let [environments (-> environments
                         (assoc-in [:test :clj-env] :test)
                         (assoc-in [:staging :clj-env] :staging)
                         (assoc-in [:prod :clj-env] :prod)
                         (assoc-in [:dev :clj-env] :dev))
        {:keys [multimethod-namespaces] :as app-config} (assoc (make-app-config environments)
                                                               :db-config db-config)

        _ (when-let [timbre-log-level (:timbre-log-level app-config)]
            (timbre/merge-config! {:level timbre-log-level
                                   :middleware [middleware]}))
        ig-system-config (make-ig-system-config app-config)
        aum-config {:app-config :app-config
                    :ig-system-config ig-system-config}]
    (timbre/info (into [] (load-namespaces (concat aum-multimethod-namespaces multimethod-namespaces))))
    aum-config))

(def ig-system nil)

(defn go
  "For production use. The ig-system var could be used to halt, reconfigure and
  restart the system in production"
  [{:keys [ig-system-config]}]
  (let [system (ig/init ig-system-config)]
    (alter-var-root #'ig-system (constantly system))))

(defn restart [{:keys [ig-system-config]}]
  (ig/halt! ig-system)
  (ig/init ig-system-config))

