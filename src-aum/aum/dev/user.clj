(ns aum.dev.user
  (:require  [weasel.repl.websocket]
             [cider.piggieback]
             [clojure.string :as str]
             [taoensso.timbre :as timbre]
             [jansi-clj.core :as jansi :refer :all :exclude [reset]]
             [clojure.pprint :refer (pprint)]
             ))

(def weasel-repl-env (weasel.repl.websocket/repl-env :ip "0.0.0.0" :port 9001))

(defonce cljs-atom (atom nil))

(defn cljs-repl []
  (let [result (cider.piggieback/cljs-repl weasel-repl-env)]
    (reset! cljs-atom result)))

(defn print-local-cp []
  (let [cp (seq (.getURLs (java.lang.ClassLoader/getSystemClassLoader)))
        cp (remove #(str/ends-with? % ".jar") cp)]
    (println (clojure.string/join "\n" cp))))


;; (print-local-cp)

;; (def version (clojure.string/trim (slurp "version")))


(timbre/info :#w "++++++++++ Loaded user namespace ++++++++++")
