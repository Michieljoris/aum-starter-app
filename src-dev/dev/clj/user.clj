(ns clj.user
  (:require
   [pagora.aum.dev.core :as dev]
   [app.database.config :refer [db-config]]
   [pagora.aum.core :as aum]
   [integrant.repl :refer [clear go halt init prep reset reset-all]]

   [bidi.bidi :as b]
   [integrant.core :as ig]
   [clojure.pprint :refer [pprint]]

   [taoensso.timbre :as timbre]))

;; (timbre/info :#w "++++++++++ Loaded dev user namespace ++++++++++")
(defn restart []
  (let [aum-config (aum/init {:db-config db-config
                              :app-config-ns 'app.config})]
    (timbre/info :#pp {:aum-config (:foo (:app-config aum-config))})

    (dev/init aum-config)
    (dev/go)
    ))

(restart)

;; (dev/go)
;; (dev/halt)
;; (dev/reset)

;; (def routes (:pagora.aum.web-server.routes/routes (dev/ig-system)))
;; (def parser (:pagora.aum.parser.core/parser (dev/ig-system)))
(let [{:keys [chsk-send!]} (:websocket (:pagora.aum.websockets.core/websocket-listener (dev/ig-system)))]
  (pprint chsk-send!)
  (chsk-send! :sente/nil-uid [:some/request-id {:name "michiel"}])
  )

;; (def version (clojure.string/trim (slurp "version")))



;; (def routes
;;   ;; ["" {"/app-path" {"" {:get :foo}
;;   ;;                   "/" {:get :foo}
;;   ;;                   "/devcards" {:post :bar}}
;;   ;;       true :not-found}
;;   ;;  ]
;;   ["" {"" {:get :foo}
;;        "/" {:get :foo}
;;        "/devcards" {:post :bar}
;;        true :not-found}]
;;   )
  ;; Routes is just a data structure:
  ;; (pprint routes)
 ;; (b/match-route routes "/devcards" :request-method :post)

 ;;  (bidi/match-route routes (bidi/path-for routes file-download) :request-method :get)
 ;;  (bidi/path-for routes file-download)


;; Examine routes, and find out route for handler
;; (match-route routes "/public/test.txt" :request-method :get)
;; (match-route routes "/public/images/background.png" :request-method :get)
;; (match-route routes "/css/garden.css" :request-method :get)

;; Example routes:
;; (def routes ["/" {"index.html" :index
;;                   "articles/" {"index.html" :article-index
;;                                "article.html" :article}}])
;; (def routes
;;   ["api/v1" {"blog" {:get
;;                      {"/index" (fn [req] {:status 200 :body "Index"})}}
;;              {:request-method :post :server-name "juxt.pro"}
;;              {"/zip" (fn [req] {:status 201 :body "Created"})}}])

;; (go)
;; (halt)
;; (def system (ig/init (make-ig-config config)))
;; (pprint system)
;; (ig/halt! system)
