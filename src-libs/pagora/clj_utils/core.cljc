(ns pagora.clj-utils.core
  (:require
   [cuerdas.core :as str]
   ))

(defn parse-natural-number
  "Reads and returns an integer from a string, or the param itself if
  it's already a natural number. Returns nil if not a natural
  number (includes 0)"
  [s]
  (cond
    (and (string? s) (re-find #"^\d+$" s)) (#?(:cljs cljs.reader/read-string)
                                            #?(:clj read-string)
                                            s)
    (and (number? s) (>= s 0))  s
    :else nil))
