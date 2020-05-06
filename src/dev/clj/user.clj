(ns clj.user
  (:require
   [pagora.aum.dev.core :as dev]
   ;; [app.database.config :refer [db-config]]
   [pagora.aum.core :as aum]
   [app.core :as app]
   ;; [pagora.aum.dev.cljs-repl :as cljs-repl]
   ;; [pagora.aum.dev.integrant.repl :refer [clear go halt init prep reset reset-all]]

   ;; [bidi.bidi :as b]
   ;; [integrant.core :as ig]
   ;; [clojure.pprint :refer [pprint]]
   [clojure.tools.namespace.repl :as n]
   [taoensso.timbre :as timbre]
   [pagora.aum.modules.db-migration.joplin.alias :refer [joplin-do]]

   [expound.alpha :as expound]
   [clojure.spec.alpha :as s]
   ))

(set! s/*explain-out* expound/printer)

(n/set-refresh-dirs "src" "libs-src" "src-frontend")
;; (n/disable-unload!)
;; (def pp pprint)
;; (n/refresh)

;; (timbre/info :#w "++++++++++ Loaded dev user namespace ++++++++++")
(defn restart []
  (let [aum-config (aum/init app/aum-params)]
    (dev/init aum-config)
    (dev/go)))

;;RESTART ====================
(restart)
;;RESTART ====================

;; DATABASE MIGRATIONS
;; See src/joplin for config, migrations and seeds
(comment
  (joplin-do :migrate {:config "joplin.edn" :env :dev :db :aum-minimal})
  (joplin-do :pending {:config "joplin.edn" :env :dev :db :aum-minimal})
  (joplin-do :rollback-n {:config "joplin.edn" :env :dev :db :aum-minimal :num "1"})
  ;; (joplin-do :rollback-id {:config "joplin.edn" :env :dev :db :sql-minimal :id "20200330150018-create-accounts"})

  (joplin-do :seed {:config "joplin.edn" :env :dev :db :aum-dev} ["seed1"])
  ;; (joplin-do :reset {:config "joplin.edn" :env :dev :db :aum-minimal})

  (joplin-do :create {:config "joplin.edn" :env :dev :db :aum-minimal :id "create-subscriptions"})
  (joplin-do :rebuild {:config "joplin.edn" :env :dev :db :aum-minimal} ["seed1"])

  )


;; (dev/go)
;; (dev/halt)
;; (dev/reset)
;; (aum/get-parser-env)
;; (timbre/info (keys (identity aum/aum-state)))

;; (let [{:keys [chsk-send!]} (:websocket (:pagora.aum.websockets.core/websocket-listener (dev/ig-system)))]
;;   (pprint chsk-send!)
;;   (chsk-send! :uid-1 [:some/request-id {:name "michiel"}])
;;   )

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
