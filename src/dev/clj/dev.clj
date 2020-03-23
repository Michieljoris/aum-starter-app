(ns dev
  (:require [app.integrations :as integrations]
            [orchestra.spec.test :as st]
            [orchestra.core :refer [defn-spec]]
            [app.core :as app-core]
            [app.redis :as redis]
            [parser.core :refer [parser-env]]
            [digicheck.common.util :as du]
            [clojure.spec.alpha :as s]
            ;; [orchestra-cljs.spec.test :as st]
            [expound.alpha :as expound]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :as tn]
            [clojure.java.io :as io]
            ;; [boot.core :refer [load-data-readers!]]
            [mount.core :as mount :refer [defstate]]
            [mount.tools.graph :refer [states-with-deps]]
            [bilby.mount.logging :refer [with-logging-status]]
            [web-server.core]

            [clojure.java.io :as io]
            ;; [app.nyse :refer [find-orders add-order]] ;; <<<< replace this your "app" namespace(s) you want to be available at REPL time
            [taoensso.timbre.appenders.3rd-party.logstash :refer [logstash-appender]]
            [taoensso.timbre :as timbre
             :refer (log  trace  debug  info  warn  error  fatal  report color-str
                          logf tracef debugf infof warnf errorf fatalf reportf
                          spy get-env log-env)]
            [app.environment :as env]
            [eftest.runner :refer [find-tests run-tests]]

            [dc-admin.backend.app.config :refer [config update-config make-logstash-appender]]
            [app.environment :as env]

            [web-server.core]
            [database.connection :as db-conn]


            [digicheck.common.timbre :refer [middleware]]

            [bilby.parser.mutate :as bilby]
            ;; https://github.com/xsc/jansi-clj
            ;; (colors)
            ;; ;; => (:black :default :magenta :white :red :blue :green :yellow :cyan)

            ;; For each color, there exist four functions, e.g. red (create a
            ;; string with red foreground), red-bright (create a string with
            ;; bright red foreground), as well as red-bg (red background) and
            ;; red-bg-bright.
            ;; (attributes
            ;; (:underline-double :no-negative :no-underline :blink-fast :no-strikethrough
            ;;               :conceal :negative :no-italic :italic :faint :no-conceal :no-bold :no-blink
            ;;               :strikethrough :blink-slow :bold :underline)
            [jansi-clj.core :as jansi :refer :all :exclude [reset]]
            [clojure.pprint :refer (pprint)]
            ))

;; (clojure.tools.namespace.repl/set-refresh-dirs "src/clj")
;; (clojure.tools.namespace.repl/refresh-all)

(timbre/merge-config! {:middleware [middleware]})

(if-let [timbre-log-level (:timbre-log-level config)]
  (timbre/merge-config! {:level timbre-log-level}))

;;Better spec error msgs
(alter-var-root #'s/*explain-out* (constantly expound/printer))
;; Eval this and see:
;; (let [f/x 42] f/x)


;; Orchestra defn-spec
;; (defn-spec arities number?
;;   ([a number?]
;;    (inc a)
;;    :a)
;;   ([a number?, b number?]
;;    (+ a b))
;;   ([a string?, b boolean?, c map?]
;;    0))

;; (defn-spec my-abs2 number?
;;   {:fn #(= (:ret %) (-> % :args :meow))}
;;   [m any?]
;;   100)

;; ; Call after defining all of your specs
;; (st/instrument)

;; (arities 1)
;; (my-abs2 200)

;; Logstash

;; To test:
;; In app.config.clj set logstash host and port to "0.0.0.0" and 12345 and enabled to true
;; Install logstash
;; In logstash dir:
;; cat > logstash.conf
;; input {
;;  tcp {
;;     port => 12345
;;     codec => json
;;   }
;; }
;; output {
;;    stdout { codec => rubydebug }
;; }
;; Start with:
; bin/logstash -f logstash.conf --config.reload.automatic
;; Eval (info "Hello logstash")
;; This should up in your terminal

(defonce setup-logstash-appender
  (timbre/merge-config! {:appenders {:logstash (make-logstash-appender)}}))
;; (info "testing bar5")

;;Possible incompatibility between sente and clojure/tools.namespace refresh
;https://github.com/ptaoussanis/sente/issues/250
;;This fixes it for now:
(def refresh-paths ["src/clj"])

(apply tn/set-refresh-dirs
       (map #(.getPath (io/file (System/getProperty "user.dir") %)) refresh-paths))

(defn eftest []
  (info "Running app tests")
  (let [app-result (run-tests (find-tests "test") {:multithread? false})
        _ (info "Running bilby tests")
        bilby-result (run-tests (find-tests "test-bilby") {:multithread? false})
        fails (+ (:fail app-result) (:fail bilby-result))
        errors (+ (:error app-result) (:error bilby-result))
        report-color (if (and (zero? fails) (zero? errors)) jansi/green jansi/red)
        ]
    (info "Result:")
    (info (report-color (str  (+ (:test app-result) (:test bilby-result)) " assertions, "
                             fails " failures, "
                             errors " errors.")))
    ;; (pprint {:app app-result :bilby bilby-result})
    ))


(comment
  ;;To run tests in some ns add this to end and eval whole ns:
 (eftest/run-tests (eftest/find-tests *ns*) {:multithread? false
                                             :fail-fast? true ;;stop on first fail
                                             :report eftest.report.pretty/report ;;no progress bar
                                             }))

(defn start [& first-time?]

  (redis/print-redis-status)

  (info (jansi/white "Starting app.."))
  (with-logging-status)
  ;; (mount/start #'app.conf/config
  ;;              #'app.db/conn
  ;;              #'app.www/nyse-app
  ;;              #'app.example/nrepl) ;; example on how to start app with certain states
  (app-core/start-mount-given-config)

  (when (and first-time? env/is-development?)

    (let [old-config (select-keys config [:http-log :sql-log :query-log])]
      (update-config (fn [c] (assoc c
                                    :http-log false
                                    :sql-log false
                                    :query-log false)))
      ;; (eftest)
      (update-config (fn [c] (merge c old-config))))))


(defn start-app-for-testing []
  (pprint "start app for testing")
  (info (jansi/white "Build-info:"))
  (pprint integrations/build-info)
  (info (jansi/white "Starting app for testing.."))
  (with-logging-status)
  ;; (mount/start #'database.connection/db-conn)

  ;; (mount/start-without
  ;;  #'parser.core/parser
  ;;  #'websockets.core/sente-router
  ;;  #'parser.core/parser-env
  ;;  #'web-server.core/web-server)
  )

(defn stop []
  (mount/stop))

(defn refresh []
  (stop)
  (tn/refresh))

(defn refresh-all []
  (stop)
  (tn/refresh-all))

(defn go
  "starts all states defined by defstate"
  []
  (start)
  :ready)

;;This is a good one. It should get app in a proper state again. I've got it on a shortcut in emacs (m)
(defn reset
  "stops all states defined by defstate, reloads modified source files, and restarts the states"
  []
  (stop)
  (tn/refresh :after 'dev/go))

(defn reset-bilby []
  (reset))

(mount/in-clj-mode)
;; http://www.dotkam.com/2015/12/22/the-story-of-booting-mount/
;; (load-data-readers!) ;TODO ???

;; ++++++++++++++++++++++++++++++++++++
;; (defonce do-start
;;   (when env/is-development?
;;     (start :first-time)))

(when env/is-testing?
  (start-app-for-testing)
  )
;; ++++++++++++++++++++++++++++++++++++


(defn p [& args]
  "Like print, but returns last arg. For debugging purposes"
  (doseq [a args]
    (let [f (if (map? a) pprint print)]
      (f a)))
  (println)
  (flush)
  (last args))


(defn str->int [s]
  (if (number? s)
    s
    (Integer/parseInt (re-find #"\A-?\d+" s))))

;; Some namespaces may fail to load, so catch any exceptions thrown
(defn- require-may-fail [ns]
  (try

   (print "Attempting to require " ns ": ")
   (require ns)
   (println "success")
   (catch Exception e (println "couldn't require " ns "\nException\n" e "\n\n"))))


;; Generally we'd want clojure.*, clojure.contrib.*, and any project-specific namespaces
;; (defn require-all-namespaces-starting-with [strng]
;;   (doall (map require-may-fail
;;               (filter #(. (str %) startsWith strng)
;;                       (find-namespaces-on-classpath)))))

;; The functions in these namespaces are so useful at the REPL that I want them 'use'd.
;; I.e. I want to be able to type 'source' rather than 'clojure.contrib.repl-utils/source'
(use 'clojure.inspector)
;; (use 'clojure.tools.trace)

;; It drives me up the wall that it's (doc re-pattern) but (find-doc "re-pattern").
;; Can use macros so that (fd re-pattern) (fd "re-pattern") and (fd 're-pattern) all mean the same thing
(defn stringify [x]
  (println "stringify given" (str x))
  (let [s  (cond (string? x) x
                 (symbol? x) (str x)
                 (and (list? x) (= (first x) 'quote)) (str (second x))
                 :else (str x)) ]
    (println (str "translating to: \"" s "\""))
    s))

;; Sometimes I like to ask which interned functions a namespace provides.
(defn ns-interns-list [ns] (#(list (ns-name %) (map first (ns-interns %))) ns))
;; Sometimes I like to ask which public functions a namespace provides.
(defn ns-publics-list [ns] (#(list (ns-name %) (map first (ns-publics %))) ns))
;; And occasionally which functions it pulls in (with refer or use)
(defn ns-refers-list  [ns] (#(list (ns-name %) (map first (ns-refers %))) ns))


;; Nice pretty-printed versions of these functions, accepting strings, symbols or quoted symbol
(defmacro list-interns
  ([]   `(pprint (ns-interns-list *ns*)))
  ([symbol-or-string] `(pprint (ns-interns-list (find-ns (symbol (stringify '~symbol-or-string)))))))
(defmacro list-publics
  ([]   `(pprint (ns-publics-list *ns*)))
  ([symbol-or-string] `(pprint (ns-publics-list (find-ns (symbol (stringify '~symbol-or-string)))))))

(defmacro list-refers
  ([]   `(pprint (ns-refers-list *ns*)))
  ([symbol-or-string] `(pprint (ns-refers-list (find-ns (symbol (stringify '~symbol-or-string)))))))

;; List all the namespaces
(defn list-all-ns [] (pprint (sort (map ns-name (all-ns)))))

;; List all public functions in all namespaces!
(defn list-publics-all-ns [] (pprint (map #(list (ns-name %) (map first (ns-publics %))) (all-ns))))

;; With all the namespaces loaded, find-doc can be overwhelming.
;; This is like find-doc, but just gives the associated names.

;; (defn- find-doc-names
;;   "Prints the name of any var whose documentation or name contains a match for re-string-or-pattern"
;;   [re-string-or-pattern]
;;     (let [re  (re-pattern re-string-or-pattern)]
;;       (doseq [ns (all-ns)
;;               v (sort-by (comp :name meta) (vals (ns-interns ns)))
;;               :when (and (:doc ^v)
;;                          (or (re-find (re-matcher re (:doc ^v)))
;;                              (re-find (re-matcher re (str (:name ^v))))))]
;;                (print v "\n"))))





;;find symbol or string in docs
(defmacro fd [symbol-or-string] `(find-doc (stringify '~symbol-or-string)))

(defmacro fdn [symbol-or-string] `(find-doc-names (stringify '~symbol-or-string)))



;;debugging macro                                try: (* 2 (dbg (* 3 4)))
(defmacro dbg [x] `(let [x# ~x] (do (println '~x "->" x#) x#)))

;;and pretty-printing version
(defmacro ppdbg [x]`(let [x# ~x] (do (println "--")(pprint '~x)(println "->")(pprint x#) (println "--") x#)))


;;and one for running tests
(defmacro run-test [fn] `(test (resolve '~fn)))


;; Sometimes it's nice to check the classpath
(defn- get-classpath []
   (sort (map (memfn getPath)
              (seq (.getURLs (java.lang.ClassLoader/getSystemClassLoader))))))

(defn print-classpath []
  (pprint (get-classpath)))

(defn get-current-directory []
  (. (java.io.File. ".") getCanonicalPath))


;;require everything from clojure and  so that find-doc can find it
;; (require-all-namespaces-starting-with "clojure")

(defn print-info []
  ;; print the classpath
  (println "Classpath:")
  (print-classpath)

  (println "Current Directory" (get-current-directory))

  ;;print the public functions in the current namespace
  (println "Current Namespace")
  (list-publics)

  ;;hint on how to require project specific namespaces
  (println "To require all namespaces starting with bla:")
  (println "(require-all-namespaces-starting-with \"bla\")")
  (println ""))



;; (require
;;  ;; '[digicheck.database.connection :refer [make-db-connection]]
;;  '[parser.core :refer [parser-env]])

(defn get-all-columns-of-table [table-name]
  (mapv (comp keyword du/underscore->hyphen)
   (keys (get-in (:raw-schema parser-env) [(keyword table-name) :columns]))))

;; (get-all-columns-of-table "translations")


;; (dlet [a 1
;;        b 2])

;; Debug reader tags.
;; Deps are not added to build.boot. But can be added to local profiles

;; https://github.com/dgrnbrg/spyscope
;; Spy reader tags
;; (take 20 (repeat #spy/p (+ 1 2 3)))

;; https://github.com/jsofra/data-scope
;; UI inspect
;; (let [data [{:a 4 :b [4 5] :c [{:b 4 :n {:v {:d {:f [4 4]}}}}]}]]
;;         #ds/i data)

;; UI table
;; (let [data [{:first-name "James" :last-name "Sofra" :age 36} {:first-name "Ada" :last-name "Lovelace":age 201 }]]
;;         #ds/it data)

;; pretty print
;; (let [data [{:a 4 :b (range 20) :c [{:b 4 :n {:v {:d {:f [4 4]}}}}]}]]
;;         #ds/pp data)

;; Not working
;; (->> [1 2 3]
;;      #ds/pp->> (map (fn [x] (inc x)))
;;      (map (fn [x] (* x x))))


;; print table
;;  (let [data [{:first-name "James" :last-name "Sofra" :age 36} {:first-name "Ada" :last-name "Lovelace":age 201 }]]
;;         #ds/pt data)

;; Install graphviz first
;; (let [data {:a [:b :c]
;;                   :b [:c]
;;                   :c [:a]}]
;;         #ds/graph data)

;; (let [data [[1 [2 3]] [4 [5]]]]
;;         #ds/tree data)

;; And more.


;; (require '[ring.mock.request :as mock])
;; (def req (mock/request :post "/api/endpoint" "some body"))
;; (:body req)
;; (slurp (:body req))

;; (obs/resource config {:method :get :path "/system/companies/logos/000/000/010/original/Screenshot_from_2018-01-24_17-21-35.png"})



;; (pprint (obs/resource config {:method :put :resource tempfile
;;                               :path "/system/companies/logos/000/000/001/original/logo-foo-bar.jpg"}))


;; (require 'sc.api)



;; (def my-fn
;;   (let [a 23
;;         b (+ a 3)]
;;     (fn [x y z]
;;       (let [u (inc x)
;;             v (+ y z u)]
;;         (* (+ x u a)
;;           ;; Insert a `spy` call in the scope of these locals
;;           (sc.api/spy
;;             (- v b)))
;;         ))))
;; (my-fn 3 4 5)
;; (sc.api/letsc 3
;;   [a b u v x y z])
