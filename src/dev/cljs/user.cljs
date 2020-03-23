(ns ^:figweel-load ^:figwheel-hooks cljs.user
  (:require
   [om.next :as om]
   ;; [app.environment :refer [is-environment]]
   [goog.log :as glog]
   [debug.mutate]
   [weasel.repl :as repl]
   [cljs.reader :refer [read-string]]
   ;; [app.environment :refer [is-development?]]
   [bilby.frontend.util :refer [om-data]]

   [reconciler.core :refer [reconciler]]
   [components.root-component :refer [RootComponent]]
   [reconciler.app-state :refer [app-state]]
   [cljs.pprint :refer [pprint]]

   [taoensso.timbre :as timbre]
   [cuerdas.core :as str]
   ))

(def debug-settings
  {:root-component :props
   :dossier-type-page true
   :dossier-type-form true
   :dossier-type-selection :true
   :dossier-type-dossier-statuses :true
   :dossier-type-checklist-statuses true
   :dossier-type-dossier-field true
   :dossier-type-name-formatter true
   :dossier-type-statuses true
   :dossier-type-name true
   :dossier-type-blocks true

   :support-question-page true
   :support-question-form true
   :support-question-list true
   :support-question-list-item true

   :user-page true
   :user-form true
   :user-list true
   :user-list-item true

   :translation-page true
   :translation-form :props
   :translation-list true
   :translation-list-item true

   :group-page true
   :group-form true
   :group-list true
   :group-list-item true

   :company-page true
   :company-list true
   :company-list-item true
   :company-form true

   :job-offer-page true
   :job-offer-form true
   :job-offer-list true
   :job-offer-list-item true

   :pdf-options-page true
   :pdf-options-form true
   :pdf-options-list true
   :pdf-options-list-item true

   :query-page [:props]


   :checklist-templates-page true
   :question :true
   :editable-list true
   :qbucket-entry true
   :root-qbucket-list true

   :test-page :props
   :default-page true

   })

;; (defonce connect-to-cljs-repl
;;   (when-not (repl/alive?)
;;     (timbre/info "Connecting to weasel repl")
;;     (repl/connect "ws://localhost:9001")))
;; To start it do (cljs/start-repl) in the editor's nrepl

(defn ^:after-load my-after-reload-callback []
  "Gets called on source changes"
  (timbre/info :#g "=============== RELOAD ===============")
  (do
    (when (om/mounted? (om/class->any reconciler RootComponent))
      (do
        ;;Because currently we add a callback to computed in root component, if we
        ;;force the root component to update all the child components that pass on
        ;;computed with that fn will also update
        ;; (.forceUpdate (om/class->any reconciler RootComponent))

        ;; Alternatively we can query for client/on-reload-key in root cmp and
        ;; pass that along in computed, or add it to queries for the components we
        ;; want to rerender on reload. Again, only cmps that pass along computed with
        ;; this reload key will update.
        (let [uuid (random-uuid)]
          ;; (timbre/info :#g "uuid:" uuid)
          (swap! app-state assoc :client/reload-key uuid))

        ;;  Doesn't work with react 16+
        ;; (rerender-react-tree reconciler)
        )
      ;; (put! (get-or-make-channel) {:event :on-jsload :data {}}
      ;;       #(timbre/info "Sent msg :on-jsload"))
      )))

(enable-console-print!)

(defn debug-om
  "Add this anywhere in the components and set the debug-flag in the def
  above. Set to false to have no debug output, set to true to get
  confirmation if cmp gets rendered, set :computed, :state or :props
  to have these printed out, or set to a vector of a selection or all
  of them"
  [arg]
  (let [[this prefix debug-flag] arg
        debug-flag (if (nil? debug-flag)
                     prefix
                     (keyword (str (name prefix) "-" (name debug-flag))))
        debug (get debug-settings debug-flag)]

    (when debug
      (timbre/info :#g (str "Rendering " debug-flag "========================"))
      (when (or (keyword? debug) (vector? debug))
        (let [flags (if (vector? debug) debug [debug])
              ;; {:keys [props computed state]} (om-data this)
              props (select-keys (om-data this) [:props])
              computed (select-keys (om-data this) [:computed])
              state (select-keys (om-data this) [:state])
              ]
          (doseq [flag flags]
            (case flag
              :props (do
                       ;; (js/console.log (str debug-flag " props:"))
                       (js/console.log props))
              :props-pp (do
                          ;; (js/console.log (str debug-flag " props:"))
                          (timbre/info :#pp props))
              :computed (do
                          ;; (js/console.log (str debug-flag " computed"))
                          (js/console.log computed))
              :computed-pp (do
                             ;; (js/console.log (str debug-flag " computed"))
                             (timbre/info :#pp computed))
              :state (do
                       ;; (js/console.log (str debug-flag " state"))
                       (js/console.log state))
              :state-pp (do
                          ;; (js/console.log (str debug-flag " state"))
                          (timbre/info :#pp state))
              nil)))))))

;; (remove-tap debug-om)

(defonce debug-om-tap
  (add-tap debug-om))
;; (js/console.log "hello")
;; (js/alert "hello")

;; (defonce glog-handler
;;   ;;You can only muck around with goog debuggers if goog.DEBUG is true
;;   ;;To set goog.DEBUG to false add
;;   ;;:closure-defines {"goog.DEBUG" false}
;;   ;;to cljs compiler options, for example in dev.cljs.edn
;;   (when ^boolean goog.DEBUG
;;     ;;If goog.DEBUG==true then om.next does (.setCapturing (Console.) true),
;;     ;;which adds console handler to root logger. Which means it'll always log.
;;     ;;Some hackish code here to remove this handler again, so we can add our own handler.
;;     (let [om-next-logger (glog/getLogger "om.next")
;;           root-logger (.getRoot goog.debug.LogManager)
;;           handler (aget (.-handlers_ root-logger) 0)]
;;       (.removeHandler root-logger handler)
;;       (.addHandler om-next-logger (fn [log-entry]
;;                                     (let [msg (.-msg_ log-entry)]
;;                                     (let [t (second (re-matches #"transacted (.*), #uuid.*" msg))
;;                                           t (str/ltrim t "'")]
;;                                       (if t
;;                                         ;;We're using tx-listen for transactions. See reconciler.core
;;                                         (js/console.log (str "%c" (with-out-str (pprint (read-string t)))) "color:green")
;;                                         (timbre/info :#g msg)))
;; ))))))

