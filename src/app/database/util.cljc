(ns app.database.util
  #?(:cljs (:require-macros [pagora.aum.database.validate.Rule :refer [rule]]))
  (:require
   [pagora.aum.database.validate.core :as bv :refer [Rules]]
   #?(:clj [pagora.aum.database.validate.Rule :refer [rule]])
   [pagora.clj-utils.core :as cu]
   [pagora.aum.database.query :refer [sql]]
   ))

(def not-deleted-scope
  [:or [[:deleted := 0] [:deleted :is :null]]])
