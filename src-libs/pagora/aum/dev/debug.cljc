(ns  pagora.aum.dev.debug
  (:require
   [taoensso.timbre :as timbre]
   [cuerdas.core :as str]
   #?@(:clj  [[taoensso.tufte :as tufte :refer (defnp p profiled profile)]]
       :cljs [[taoensso.tufte :as tufte :refer-macros (defnp p profiled profile)]])
   ))


;; (tufte/add-basic-println-handler! {})

(defn now-in-ms []
  #?(:clj  (System/currentTimeMillis)
     :cljs (system-time)))

(defn mark-point [p & args]
  #?(:cljs
     (let [from (first @js/window.foo)
           start (-(.getTime (js/Date.)) from)]
       (swap! js/window.foo conj (concat [p start] args)))))

(defn reset-points []
  #?(:cljs
     (aset js/window "foo" (atom [(.getTime (js/Date.))]))))

(defn reset-accumulator []
  #?(:cljs
     (aset js/window "accumulator" (atom 0))))

(reset-accumulator)

(defn add-to-accumulator [ms]
  #?(:cljs
     (swap! js/window.accumulator + ms)))

(defn print-accumulator []
  #?(:cljs
     (js/setTimeout (fn []
                      (timbre/info :#r "--------------------------")
                      (timbre/info :#pp @js/window.accumulator)
                      (timbre/info :#r "--------------------------")
                      ) 1000)))


(defn print-points []
  #?(:cljs
     (js/setTimeout (fn []
                      (timbre/info :#r "--------------------------")
                      (timbre/info :#pp @js/window.foo)
                      (timbre/info :#r "--------------------------")
                      ) 1000)))

(defn warn-when [s max some-str]
  (let [e (now-in-ms)
        dt (- e s)
        dt #?(:cljs (js/Math.round dt) :clj dt)]
    (when (< 16 dt)
      (timbre/warn (str some-str " took " dt " ms")))
    dt
    ))
