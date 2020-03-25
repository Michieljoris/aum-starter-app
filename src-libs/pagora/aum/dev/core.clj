(ns pagora.aum.dev.core
  (:require
   [clojure.string :as str]
   [taoensso.timbre :as timbre]
   [integrant.core :as ig]
   [integrant.repl :as ig-repl]
   [pagora.aum.integrant :refer [make-ig-config]])
  )

(defn print-local-cp []
  (let [cp (seq (.getURLs (java.lang.ClassLoader/getSystemClassLoader)))
        cp (remove #(str/ends-with? % ".jar") cp)]
    (println (clojure.string/join "\n" cp))))

;; (timbre/info :#w "++++++++++ Loaded user namespace ++++++++++")
(defn init [config]
  (let [ig-config (make-ig-config config)]
    (timbre/info (into [] (ig/load-namespaces ig-config)))
    (ig-repl/set-prep! (constantly ig-config))
    ))

;; (ig-repl/halt)
;; (ig-repl/clear)
;; (print-local-cp)

;; (def version (clojure.string/trim (slurp "version")))
