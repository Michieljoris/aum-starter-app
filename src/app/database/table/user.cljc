(ns app.database.table.user
  #?(:cljs (:require-macros [pagora.aum.database.validate.Rule :refer [rule]]))
  (:require [pagora.aum.database.validate.core :as bv :refer [Rules]]
            [pagora.aum.database.validate.rules :as rule :refer [require-keys]]
            #?(:clj [pagora.aum.database.validate.Rule :refer [rule]])
            [pagora.aum.database.query :refer [sql]]
            [taoensso.timbre :as timbre]
            ))

;; (dev/get-all-columns-of-table "users")

(def schema {:id :int
             :name :text
             :email :text
             :deleted {:type :boolean}})

(def config {:root true
             :schema schema
             :columns (keys schema)
             :read {:blacklist []}})

(defmethod bv/validate ["no-role" :create :user]
  [_ _ {:keys [user]} _ new-record _]

  )
