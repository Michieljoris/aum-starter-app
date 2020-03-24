(ns pagora.aum.backend.web-server.routes
  (:require
   ;; [web-server.file-transfer :as transfer]
   ;; [websockets.core :refer [sente-route]]

   [pagora.aum.backend.web-server.response :as resp]

   [integrant.core :as ig]
   [clojure.java.io :as io]
   [cuerdas.core :as str]
   [clojure.pprint :refer [pprint]]
   [taoensso.timbre :as timbre :refer [info]]
   )
  (:import java.net.NetworkInterface))

(def ip
  (try
    (let [ifc (NetworkInterface/getNetworkInterfaces)
          ips (->> (enumeration-seq ifc)
                   (mapv #(bean %))
                   (filter #(false? (% :loopback)))
                   (mapv (fn [e]
                           (as-> (:interfaceAddresses e) v
                                 (.split (str v) " ")
                                 (first (nnext v))
                                 (if v (str (second (.split v "/"))) "")
                                 )))
                   (filter #(re-matches #"(^192\.168\..*)|(^10\..*)" %)))]
      (first ips))
    (catch Exception e
      ;; (info e)
      nil)))

;; Inserts script tag for vorlon.js using ip of first local lan interface if vorlon-script is true in config
;; Install vorlon: http://vorlonjs.com/
;; Run vorlon on commandline
;; Open vorlorn dashboard at localhost:1337

;; Other ways to load vorlon.js:
;; <!-- http://stackoverflow.com/questions/37020588/embedded-devices-javascript-debugging/37163704#37163704 -->
;; <!-- <script src="http://f48e6e75.ngrok.io/vorlon.js"></script> -->
;; <!-- <script src="http://localhost/vorlon.js"></script> -->

(defn make-html-string [config html-file-name]
  (timbre/info "getting:" html-file-name)
  (when (:vorlon-script config)
    (info (str "Navigate to http://" ip ":" (:server-port config) " on mobile device for vorlon remote debugging")))

  (cond-> (slurp (io/resource html-file-name))
    (:vorlon-script config) (str/replace "<!--vorlon-->"
                                         (str "<script src=\"http://" ip ":1337/vorlon.js\"></script>"))
    true (str/replace "<!--vorlon-->" "")))

(defn html-file-response [config html-file-name]
  (fn [request]
    {:status  200
     :headers {"Content-Type"  "text/html; charset=utf-8"
               "Cache-Control" "no-cache"}
                                        ;:cookies (create-cookie request)
     :body    (make-html-string config html-file-name)
     }))

(defn aum-routes [{:keys [app-html-file-name devcards-html-file-name] :as config}]
  {:get {"/"         (html-file-response config app-html-file-name)
         ;; "/file-download/" {:get [[true (partial transfer/file-download ["admin_new" "file-download"])]]}
         "/devcards" (html-file-response config devcards-html-file-name)}
   ;; :post {"/file-upload" (resp/wrap-authenticate transfer/file-upload)}
   })

  ;; Routes
(defn make-routes [{:keys [app-path extra-routes] :as config
                    :or {extra-routes (constantly {})}}]
  (let [app-path (str/trim app-path "/")]
    ["/" {app-path  (merge (aum-routes config)
                           ;; (sente-route config)
                           (extra-routes config)
                           {true resp/not-found})}]))

(defmethod ig/init-key ::routes [_ config]
  (make-routes config))

  ;; Routes is just a data structure:
  ;; (pprint routes)
 ;; (bidi/match-route routes "/admin_new/file-upload" :request-method :post)
  ;; (bidi/match-route routes "/admin_new/file-download/system/companies/logos/000/000/010/original/Screenshot_from_2018-01-24_17-22-05.png" :request-method :get)
 ;; (bidi/match-route routes "/admin_new/devcards" :request-method :get)

 ;;  (bidi/match-route routes (bidi/path-for routes file-download) :request-method :get)
 ;;  (bidi/path-for routes file-download)

;; => "/admin_new/file-upload/group-logo"

;; Examine routes, and find out route for handler
;; (match-route routes "/public/test.txt" :request-method :get)
;; (match-route routes "/public/images/background.png" :request-method :get)
;; (match-route routes "/css/garden.css" :request-method :get)
;; (match-route routes "/api/v1/dashboard/answers" :request-method :get)
;; => {:handler #'web-server.response/dashboard-answers, :request-method :get}
;; (bidi/path-for routes :csv)
;; => "/api/v1/dashboard/answers"

;; Example routes:
;; (def routes ["/" {"index.html" :index
;;                   "articles/" {"index.html" :article-index
;;                                "article.html" :article}}])
;; (def routes
;;   ["api/v1" {"blog" {:get
;;                      {"/index" (fn [req] {:status 200 :body "Index"})}}
;;              {:request-method :post :server-name "juxt.pro"}
;;              {"/zip" (fn [req] {:status 201 :body "Created"})}}])
