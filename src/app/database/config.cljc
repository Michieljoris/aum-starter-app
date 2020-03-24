(ns app.database.config
  (:require [app.database.table.user :as user]))

(def db-config {:user user/config})
