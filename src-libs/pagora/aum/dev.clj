(ns pagora.aum.dev
  (:require  [weasel.repl.websocket]
             [cider.piggieback]
             [clojure.string :as str]
             [taoensso.timbre :as timbre]
             [integrant.core :as ig]
             [integrant.repl :as ig-repl]
             [pagora.aum.backend.integrant :refer [make-ig-config]]))

(def weasel-repl-env (weasel.repl.websocket/repl-env :ip "0.0.0.0" :port 9001))

(defonce cljs-atom (atom nil))

(defn cljs-repl []
  (let [result (cider.piggieback/cljs-repl weasel-repl-env)]
    (reset! cljs-atom result)))

(defn print-local-cp []
  (let [cp (seq (.getURLs (java.lang.ClassLoader/getSystemClassLoader)))
        cp (remove #(str/ends-with? % ".jar") cp)]
    (println (clojure.string/join "\n" cp))))

(defn init [config]
  (let [ig-config (make-ig-config config)]
    (timbre/info (into [] (ig/load-namespaces ig-config)))
    (ig-repl/set-prep! (constantly ig-config))
    ))



;; (ig-repl/halt)
;; (ig-repl/clear)
;; (print-local-cp)

;; (def version (clojure.string/trim (slurp "version")))


;; (timbre/info :#w "++++++++++ Loaded user namespace ++++++++++")
