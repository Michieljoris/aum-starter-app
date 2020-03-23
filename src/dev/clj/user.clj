(ns user
  (:require  [weasel.repl.websocket]
             [cider.piggieback]
             ;; [app.environment :refer [environment]]
             
             [clojure.string :as str]
             ;; [dev]
             [taoensso.timbre :as timbre]
             [jansi-clj.core :as jansi :refer :all :exclude [reset]]
             [clojure.pprint :refer (pprint)]
             ;; [core :as core]
             ))

(def weasel-repl-env (weasel.repl.websocket/repl-env :ip "0.0.0.0" :port 9001))

(defonce cljs-atom (atom nil))

(defn cljs-repl []
  (let [result (cider.piggieback/cljs-repl weasel-repl-env)]
    (reset! cljs-atom result)))

(defn local-cp []
  (let [cp (seq (.getURLs (java.lang.ClassLoader/getSystemClassLoader)))
        cp (remove #(str/ends-with? % ".jar") cp)]
    (println (clojure.string/join "\n" cp))))

(timbre/info :#w "++++++++++ Loaded user namespace ++++++++++")

(local-cp)

;; (def version (clojure.string/trim (slurp "version")))

;; (info (jansi/white "Build-info:"))
;; (timbre/info {:version version
;;               :env environment
;;               :tab (shell/sh "git" "rev-parse" "--abbrev-ref" "HEAD")
;;               ;; :tag (git describe --abbrev=0 --tags HEAD)
;;               })

;; (try
;;   (write-build-info (str "resources/admin_new/" "build.json")
;;                     {:version version
;;                      :env environment
;;                      :tag (git describe --abbrev=0 --tags HEAD)
;;                      })
;;   (catch Exception e
;;     (timbre/info e)
;;     ))


;; (pprint integrations/build-info)

;; (dev/start)
