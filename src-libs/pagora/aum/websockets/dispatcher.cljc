(ns pagora.aum.websockets.dispatcher
  (:require
   [common.util :as u]

   [pagora.clj-utils.core :as cu]
   [pagora.aum.security :as security]
   [parser.core :as p]
   [app.security :as app-security]
   [om.next.impl.parser :as omp]


   
   #?@(:clj [[app.integrations :as integrations]
             [web-server.dc-get :refer [dc-get]]
             [websockets.sente :as sente]
             [bilby.test.snapshot :as snapshot]
             [com.climate.newrelic.trace :refer [defn-traced]]
             [clojure.java.io :as io]])

   [clojure.pprint :refer [pprint]]
   [taoensso.timbre :as timbre :refer [debugf info]]))

(defn parser [] #?(:clj p/parser :cljs @p/parser))
(defn parser-env [] #?(:clj p/parser-env :cljs @p/parser-env))

(defn get-user-by-ev-msg
  "Checks if remember token in db for uid is still valid, if so,
  returns user associated"
  [env {:as ev-msg :keys [uid ring-req ?reply-fn]}]
  (let [user (security/get-user-by-remember-token env ring-req)
        user-id (:id user)]
    (when (and (not= uid :uid-nil) (= (u/user-id->uid user-id) uid))
      user)))

;; (reset! sente/debug-mode?_ true) ; Uncomment for extra debug info

;; Sente multimethod event handler, dispatches on event-id
(defmulti event-msg-handler :id)

;; Root handler
(defn event-msg-handler* [{:as ev-msg :keys [id ?data event]}]
  ;; (timbre/infof "EVENT: %s" event)
  ;; Debug here
  (event-msg-handler ev-msg)) ;; Call multimethod

;; Handlers ----------

;; Handshake
(defmethod event-msg-handler :chsk/handshake
  [{:as ev-msg :keys [?data]}]
  (let [[?uid ?csrf-token ?handshake-data] ?data]
    (debugf "Handshake: %s" ?data)))

;; Pinging from client
(defmethod event-msg-handler :chsk/ws-ping
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    ;; (debugf "Ping event: %s" event)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-from-server event}))))

(defn mutation? [query]
  (some (comp symbol? :dispatch-key) (:children (omp/query->ast query))))

(defn send-response [uid reply-fn query response]
  (when (get-in (config) [:query-log])
    (info :#w (if (mutation? query) "Mutation" "Read")" reply sent ")
    (when env/is-development? (pprint response)))
  (reply-fn response))

;; "Process a query (read/mutation) from client"
(#?(:clj defn-traced :cljs defn)
 handle-admin-query
 [{:as ev-msg :keys [?data ?reply-fn uid]}]
 (do ;; try
   (let [query      ?data
         query-type (if (mutation? query) "Mutation" "Read")
         parser-env (parser-env)]

     (when (get-in (config) [:query-log])
       (if env/is-development?
         (do
           (info :#w query-type " received: ")
           (pprint query))
         (info :#w query-type " received: " query)))

     ;; Parser might set status to something other than :ok
     (reset! (:state parser-env) {:status :ok})

     (let [user     (get-user-by-ev-msg parser-env ev-msg)
           response {:value (if user
                              (let [user-role (app-security/calc-role parser-env user)
                                    user      (select-keys user (security/get-whitelist parser-env :read :user
                                                                                        (assoc user :role user-role)))
                                    user      (app-security/process-user parser-env user user-role)]
                                ( (parser) {:user user
                                            :push (fn [response]
                                                    ;; (timbre/info (str "Pushing response to " uid ":"))
                                                    (pprint response)
                                                    ;;TODO-MERGE: mock this in
                                                    ;;mock-mode. This is used for
                                                    ;;lawcat/chin responses
                                                    #?(:clj
                                                       (sente/chsk-send! uid [:admin/broadcast response])))
                                            } query))
                              {:authenticated? false})}]
       (when ?reply-fn                  ;query
         (let [state    (-> parser-env :state deref)
               response (merge (select-keys state [:status :table-data :original-table-data :warnings :tempids])
                               response)]
           (if (:simulate-network-latency (config))
             (do
               (info :#y "Simulating network latency of " (/ (:latency (config)) 1000.0) " seconds")
               #?(:clj (Thread/sleep (:latency (config))))
               (send-response uid ?reply-fn query response))
             (send-response uid ?reply-fn query response))))
       ))
   ;; (catch #?(:clj Exception :cljs :default) e
   ;;     (timbre/info e)
   ;;   (let [{:keys [msg context stacktrace] :as error} (du/parse-ex-info e)]
   ;;     (?reply-fn
   ;;      {:status :error
   ;;       :value {:message msg :query ?data :context context :stacktrace [:not-returned]}})))
   ))

(defmethod event-msg-handler :admin/get-tent-question [{:keys [?data ?reply-fn]}]
  (timbre/info "?data" ?data)
  ;;TODO validate (:id ?data) is a number!!!
  (let [id (du/parse-natural-number (:id ?data))]

    #?(:cljs (timbre/info "TODO-MERGE mock tent-question response")
       :clj (dc-get (str "/api/v2/tent_questions/" id) (fn [result]
                                                         (timbre/info result)
                                                         (?reply-fn result))))))
(defmethod event-msg-handler :admin/query [ev-msg]
  (handle-admin-query ev-msg))

(defmethod event-msg-handler :admin/list-table-cols
  [{:as ev-msg :keys [?data ?reply-fn]}]
  (let [{:keys [table type]} ?data
        user (get-user-by-ev-msg p/parser-env ev-msg)]
    (?reply-fn (security/get-whitelist p/parser-env type table user))))

;; (security/get-whitelist (assoc p/parser-env :db-config database.config/db-config) :read :question {:id 1 :role "group-admi"})

#?(:clj
   (defmethod event-msg-handler :admin/get-build
     [{:as ev-msg :keys [?data ?reply-fn]}]
     (?reply-fn integrations/build-info)))

(def snapshot-file-name "/test/cljs/tests/snapshot_data.cljs" )

#?(:clj
   (defmethod event-msg-handler :tests/update-snapshot
     [{:as ev-msg :keys [?data ?reply-fn]}]
     (let [{:keys [path actual-result]} ?data]
       (info ?data)
       (snapshot/update-snapshot-data snapshot-file-name path actual-result)
       (?reply-fn :ok))))

(defmethod event-msg-handler :admin/test
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn uid connected-uids]}]
  (info "Test received!!!")
  ;; (pprint ev-msg)
  (info ?data)
  (info "meta:" (meta ?data))
  (when ?reply-fn
    (?reply-fn (:test-received ?data))))

(defmethod event-msg-handler :admin/login
  [{:keys [?data ?reply-fn]}]
  (when env/is-development?
    (timbre/info "Login:" (assoc ?data :password "***"))
    (let [parser-env (parser-env)
          user (security/login parser-env ?data)
          user-role (app-security/calc-role parser-env user)
          response {:authenticated (boolean (:remember-token user))
                    :remember-token (:remember-token user)
                    :user-role user-role}]
      (when ?reply-fn
        ;; (timbre/info response)
        (?reply-fn response)))))

(defmethod event-msg-handler :admin/logout [{:keys [?reply-fn] :as ev-msg}]
  (let [parser-env (parser-env)
        user (get-user-by-ev-msg parser-env ev-msg)
        response {:response (security/logout parser-env user)}]
    (when ?reply-fn
      ;; (timbre/info response)
      (?reply-fn response))))

;; (defn find-handler-fn
;;   "Every msg from sente has an id, here we hackishly look for a var in
;;   this ns that has the same name"
;;   [sente-id]
;;   (info "sente-id" sente-id)
;;   (ns-resolve (:ns (meta #'event-msg-handler)) (symbol (name sente-id))))

;;TODO-CLJC
;; ev-msg map to spoof for frontend:
;; (timbre/info :#r (event-msg-handler {:id :admin/login
;;                                      ;; :uuid 1
;;                                      ;; :ring-req {}
;;                                      :?data {:user "foo user" :password "abc"}
;;                                      :?reply-fn (fn [data] (timbre/info data))}))
;; (get-in req [:cookies "remember_token" :value])

(defmethod event-msg-handler :default
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn uid connected-uids]}]
  (let [session (:session ring-req)
        ;; uid     (:uid     session)
        ]
    ;; (info "EVENT in event-msg-handler")
    ;; (info {:id id
    ;;      :uid uid
    ;;      :event event
    ;;      :data ?data
    ;;      :cookies (:cookies ring-req)
    ;;      :connected-uids connected-uids
    ;;      :session session
    ;;      :reply-fn ?reply-fn
    ;;      })
    ;; ;;
    (if-let [handler-fn false ;; (find-handler-fn id)
             ;; authorized? (authorize-ev-msg ev-msg)
             ]
      ;; if handler-fn                           ;we've got a handler for it
      (handler-fn ev-msg)
      ;; (if authorized?
      ;;   (handler-fn ev-msg)
      ;;   (when ?reply-fn               ;if not authorized suggest client set current-user to nil
      ;;     (info "Unauthorized, but trying to query:" ?data)
      ;;     (?reply-fn {:current-user nil}))
      ;;   )

      (do
        (debugf "Unhandled sente id: %s" event)
        (when ?reply-fn     ;let client know he's sent something unprocessable
          (?reply-fn {:umatched-event-as-echoed-from-from-server event}))))))



;; ;;  ; Note that this'll be fast+reliable even over Ajax!:
;; (defn test-fast-server>user-pushes []
;;   (doseq [uid (:any @connected-uids)]
;;     (doseq [i (range 100)]
;;       (chsk-send! uid [:fast-push/is-fast (str "hello " i "!!")]))))

;; (comment (test-fast-server>user-pushes))

;; ;; ;;;; Example: broadcast server>user

;; ;; ;; As an example of push notifications, we'll setup a server loop to broadcast
;; ;; ;; an event to _all_ possible user-ids every 10 seconds:

;; (defn start-broadcaster! []
;;   (go-loop [i 0]
;;     (<! (async/timeout 10000))
;;     (println (format "Broadcasting server>user: %s" @connected-uids))
;;     (doseq [uid (:any @connected-uids)]
;;       (chsk-send! uid
;;                   [:some/broadcast
;;                    {:what-is-this "A broadcast pushed from server"
;;                     :how-often    "Every 10 seconds"
;;                     :to-whom uid
;;                     :i i}]))
;;     (recur (inc i))))

;; (comment (start-broadcaster!))

;; (try
;;   (json/read-str "")
;;   (catch Exception e
;;     (timbre/warn (.toString e))))
