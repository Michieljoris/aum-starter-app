(ns pagora.aum.websockets.sente
  (:require [pagora.aum.security :as security]
            [pagora.aum.util :as au]
            [taoensso.sente :as sente]
            [taoensso.timbre :as timbre]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]))

(defn get-user-id
  "Returns sente user id (eg uid-1), deduced from remember token in cookie that
  came with the request for the connection."
  [env req]
  ;; (timbre/info "Getting user id for sente!!!!!!!")
  (let [user-id (:id (security/get-user-by-remember-token env req [:id]))]
    (au/user-id->uid user-id)))

;; (get-user-id {:cookies {"remember_token" {:value "f830c933-9ee9-4e40-8796-59428f448e32"}} })

(defn user-id-fn [req]
  (get-user-id parser-env req))

(def chsk-server (sente/make-channel-socket! (get-sch-adapter)
                                                 {:packer :edn
                                                  :user-id-fn user-id-fn}))

(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]} chsk-server]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )
