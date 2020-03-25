(ns pagora.aum.websockets.core
  (:require
   [websockets.sente :as sente]
   [taoensso.sente :as taensso-sente]
   [taoensso.timbre :as timbre]
   [websockets.dispatcher :refer [event-msg-handler*]]))

;; ;; debug fn
(defn sente? []
  (sente/chsk-send! :taoensso.sente/nil-uid [:some/request-id {:name "michiel"}])
  )

;; We can watch this atom for changes if we like
(add-watch sente/connected-uids :connected-uids
  (fn [_ _ old new]
    (when (not= old new)
      (timbre/infof "Connected uids change: %s" new))))

(def sente-route
  {"/chsk" {:get { "" sente/ring-ajax-get-or-ws-handshake }
            :post { ""  sente/ring-ajax-post }}})

(defn start-router! []
  (let [stop-fn (taensso-sente/start-chsk-router! sente/ch-chsk event-msg-handler*
                                                  {:simple-auto-threading? true})]
    ;; (info "Started sente router")
    stop-fn))

(defn stop-router! [stop-fn]
  (stop-fn)
  ;; (timbre/info "Stopped sente router")
  )

(mount/defstate ^{:on-reload :noop} sente-router
  :start (start-router!)
  :stop (stop-router! sente-router))



;; (defn start-sente []
;;   (let [{:keys [ch-recv send-fn connected-uids
;;                 ajax-post-fn ajax-get-or-ws-handshake-fn]} (sente/make-channel-socket! (get-sch-adapter)
;;                                                                                        {:packer :edn
;;                                                                                         :user-id-fn user-id-fn})
;;         stop-fn (sente/start-chsk-router! ch-recv event-msg-handler*)]

;;     (add-watch connected-uids :logger
;;                (fn [_ _ old new]
;;                  (when (not= old new)
;;                    (timbre/infof "Connected uids change: %s" new))))

;;     {:stop stop-fn
;;      :send send-fn ; ChannelSocket's send API fn
;;      :connected-uids connected-uids ; Watchable, read-only atom
;;      :route {"/chsk" {:get { "" ajax-get-or-ws-handshake-fn }
;;                       :post { ""  ajax-post-fn }}}}))

;; (defn stop-sente [{:keys [connected-uids stop]}]
;;   (remove-watch connected-uids :logger)
;;   (stop)
;;   ;; (info "Stopped sente router")
;;   )

;; (mount/defstate sente
;;   :start (start-sente)
;;   :stop (stop-sente sente))


;; ;; ;; debug fn
;; ;; (defn sente? []
;; ;;   ((:send sente) :taoensso.sente/nil-uid [:some/request-id {:name "michiel"}]))
