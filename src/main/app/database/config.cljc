(ns app.database.config
  (:require
   [pagora.aum.modules.auth.db-config.account :as account]
   [pagora.aum.modules.auth.db-config.user :as user]
   [pagora.aum.modules.auth.db-config.role :as role]
   [pagora.aum.modules.auth.db-config.subscription :as subscription]
   [pagora.aum.modules.auth.db-config.auth :as auth]
   [pagora.aum.modules.events.db-config.event :as event]

   ))

(def db-config
  {:account account/config
   :user user/config
   :role role/config
   :auth auth/config
   :subscription subscription/config
   :event event/config


   })
