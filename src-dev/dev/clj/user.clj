(ns user
  (:require
   [app.config :refer [config]]
   [pagora.aum.dev :as dev]
   [integrant.repl :refer [clear go halt init prep reset reset-all]]
   [integrant.repl.state :as ig-state]

   [bidi.bidi :as b]
   [integrant.core :as ig]
   [pagora.aum.integrant :refer [make-ig-config]]
   [clojure.pprint :refer [pprint]]
   ;; [app.backend.core]

   [taoensso.timbre :as timbre]))

(dev/init config)
(go)

;; (def routes (:pagora.aum.web-server.routes/routes ig-state/system))
;; (pprint routes)


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
