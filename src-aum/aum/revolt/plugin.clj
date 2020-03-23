(ns aum.revolt.plugin
  (:require
   [revolt.plugin :refer [create-plugin resolve-from-symbol]]
   ))

(defmethod create-plugin ::nrepl-piggieback [_ config]
  (resolve-from-symbol 'aum.revolt.plugins.nrepl-piggieback config))
