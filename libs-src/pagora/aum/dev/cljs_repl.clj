(ns pagora.aum.dev.cljs-repl
  (:require  [weasel.repl.websocket]
             [cider.piggieback]
             [clojure.string :as str]
             [taoensso.timbre :as timbre]
             [integrant.core :as ig]
             [integrant.repl :as ig-repl]
             [pagora.aum.integrant :refer [make-ig-config]]))

(def weasel-repl-env (weasel.repl.websocket/repl-env :ip "0.0.0.0" :port 9001))

(defonce cljs-atom (atom nil))

(defn cljs-repl []
  (let [result (cider.piggieback/cljs-repl weasel-repl-env)]
    (reset! cljs-atom result)))

