(ns pagora.aum.database.connection
  (:require
   [taoensso.timbre :as timbre]
   [integrant.core :as ig]

   #?(:clj [clj-time.jdbc])
   [pagora.clj-utils.database.connection :refer [make-db-connection]]
   ))

(defmethod ig/init-key ::db-conn [_ {{:keys [mysql-database]} :config}]
  (timbre/info "Making db-conn")
  (make-db-connection mysql-database))
