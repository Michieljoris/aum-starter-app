(ns pagora.aum.environment
  #?(:cljs
     (:require-macros [pagora.aum.environment :refer [environment-macro]]))
  (:require
   #?(:clj
      [environ.core :refer [env]])
   [taoensso.timbre :as timbre]))

#?(:clj
   (def environment-clj
     (or (keyword (env :clj-env)) :dev)))

#?(:clj
   (defmacro environment-macro  []
     environment-clj))

#?(:cljs
   (def environment-cljs
     (environment-macro)))

(def environment
  #?(:clj environment-clj
     :cljs environment-cljs))

(defn is-development? []
  (= environment :dev))

(defn is-staging? []
  (= environment :staging))

(defn is-production? []
  (= environment :prod))

(defn is-testing? []
  (= environment :test))
