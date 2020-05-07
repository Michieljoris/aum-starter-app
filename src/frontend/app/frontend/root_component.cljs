(ns app.frontend.root-component
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [pagora.aum.frontend.util :refer [make-cmp om-data]]
   [goog.object :as goog]
   [cuerdas.core :as str]
   [js.react :as react]
   [js.ag-grid-react :as ag-grid-class]
   [js.react-data-grid :as react-data-grid-class]
   [pagora.aum.modules.semantic.core :as s]
   ))

;; (js/console.log react-data-grid-class)

(defui ^:once Foo
  static om/IQuery
  (query [this]
    [:client/foo])
  Object
  (render [this]
    (html [:div "in foo4"])))

(def foo (make-cmp Foo))

(defn ag-grid [props]
  (let [props (clj->js props)
        create-element-fn (.-createElement react)]
    (create-element-fn ag-grid-class props)))

(defn make-column-defs [props]
  (->> props
       (mapv (fn [prop]
               {:headerName (str/capital (name prop)) :field (name prop)
                :sortable true :filter true ;; :checkboxSelection true
                }))))

(def queries {:account [:id :name]
              :user [:id :name :email]
              :role [:id :name]
              :subscription [:id]})

(defn table [this {:keys [rows table auth-tables-state]}]
  (let [columns (get queries table)
        on-selected (fn [ args]
                      (let [{:strs [api]}(js->clj args)
                            selected-rows (js->clj (.getSelectedRows api))
                            selected-id (get-in selected-rows [0 "id"])
                            read-record-kws (->> (dissoc auth-tables-state table)
                                                 (filter (fn [[_ {:keys [tables]}]]
                                                           (contains? tables table)))
                                                 keys
                                                 (mapv #(keyword (str (name %) "-records"))))]

                        (om/transact! this (into [`(aum/set-selected-auth-id {:table ~table
                                                                              :id ~selected-id})]
                                                   read-record-kws))))]
    (html
     [:div {:class "ag-theme-balham"
            :style {:height 250
                    :border "black solid 1px"
                    ;; :width 600
                    }}
      (ag-grid {:columnDefs (make-column-defs columns)
                :rowData (if (empty? rows) [] rows)
                :onSelectionChanged on-selected
                :rowDeselection true
                :onGridReady (fn [params]
                               (om/update-state! this assoc :grid-api (.-api params)))
                :rowSelection "single"})]))
  )

(defn set-table-constraints [this table selected-table args]
  (let [{:strs [checked]} (js->clj args)]
    (om/transact! this [`(aum/set-table-constraints{:table ~table
                                                    :selected-table ~selected-table
                                                    :checked ~checked})
                        (keyword (str (name table) "-records"))])))

(defui ^:once RootComponent
  static om/IQuery
  (query [this]
    [:client/reload-key
     {:account-records (get queries :account)}
     {:user-records (get queries :user)}
     {:role-records (get queries :role)}
     {:subscription-records (get queries :subscription)}
     :client/auth-tables-state
     ])
  Object
  (componentDidMount [this]
    ;; (isomorphic2)
    ;; (om/update-state! assoc :grid grid)
    )
  (render [this]
    (html
     [:div
      (let [{:keys [props state computed] :as data} (om-data this)
            {:keys [:account-records :user-records :role-records :subscription-records
                    client/auth-tables-state]} props]
        ;; (timbre/info :#pp {:data data ;; :query (om/get-query this)
        ;;                    })
        (timbre/info :#pp {:PROPS props})


        (html [:div {:class "level"}
               [:div {:class "level-item has-text-centered"}
                ;; s/container {:fluid false}
                [:div {:style {:margin "10px 100px 10px 100px"}}
                 "Accounts"
                 (s/checkbox {:label "Selected user:"
                              :onClick #(set-table-constraints this :account :user %2)})
                 (s/checkbox {:label "Selected role:"
                              :onClick #(set-table-constraints this :account :role %2)})
                 (table this {:rows account-records  :table :account :auth-tables-state auth-tables-state})
                 "Users"
                 "   Filter on:"
                 (s/checkbox {:label "Selected account:"
                              :onClick #(set-table-constraints this :user :account %2)})
                 (s/checkbox {:label "Selected role:"
                              :onClick #(set-table-constraints this :user :role %2)})
                 (table this {:rows user-records :table :user :auth-tables-state auth-tables-state})
                 "Roles"
                 (s/checkbox {:label "Selected account:"
                              :onClick #(set-table-constraints this :role :account %2)})
                 (s/checkbox {:label "Selected user:"
                              :onClick #(set-table-constraints this :role :user %2)})
                 (table this {:rows role-records :table :role :auth-tables-state auth-tables-state})
                 "Subscriptions"
                 (s/checkbox {:label "Selected account:"})
                 (s/checkbox {:label "Selected user:"})
                 (table this {:rows subscription-records :table :subscription :auth-tables-state auth-tables-state})]

                 ;; (container
                 ;;  (grid {:columns 3 :divided true}
                 ;;    (row {}
                 ;;      (column {}
                 ;;        (str "Aum starter app " (-> props :user first :name)))
                 ;;      (column
                 ;;          (button {:primary true :circular true  :onClick #(println "Hello world")} "Click me!!!")))
                 ;;    (row
                 ;;        ;; (ag-grid)
                 ;;        (column (str "Aum starter app " (-> props :user first :name)))
                 ;;      (column
                 ;;          (checkbox ;; {:primary true :circular true  :onClick #(println "Hello world")}
                 ;;           "Click me!!!")))))
                 ]]))])
    ))


;; [:div {:id "my-id"}
;;  (button {:primary true :circular true  :onClick (fn [] (println "Hello world")
;;                                                    (js/console.log @isc-grid)
;;                                                    (.destroy @isc-grid)
;;                                                    (isomorphic2)
;;                                                    ;; (.setData @isc-grid (clj->js []))
;;                                                    )} "Click me!!!")
;;  ;; (js/console.log (om/get-state this))
;;  ;; (.redraw (:grid (om/get-state this)))

;;  ]


;; (react-data-grid {:columns columns
;;                   :rowsCount 3
;;                   ;; :minHeight "150"
;;                   :rowGetter (fn [i]
;;                                (js/console.log "Hello!!!!!!!!!!!!!!!!!!!!!!!!!!!!1" i)
;;                                (clj->js (get rows i)))})


(def  columns  [{:key "id" :name "ID" }
                {:key "title" :name "Title" }
                {:key "count" :name "Count" }])

(def rows
  [{:id 0 :title "row1" :count 20} {:id 1 :title "row2" :count 40} {:id 2 :title "row3" :count 60}])


(def country-data
  [
   {
    :continent "North America",
    :countryName "United States",
    :countryCode "US",
    :area 9631420,
    :population 298444215,
    :gdp 12360.0,
    :independence (js/Date. 1776,6,4),
    :government "federal republic",
    ::government_desc 2,
    :capital "Washington, DC",
    :member_g8 true,
    :article "http //en.wikipedia.org/wiki/United_states"
    } ,
   {
    :continent "Asia",
    :countryName "China",
    :countryCode "CH",
    :area 9596960,
    :population 1313973713,
    :gdp 8859.0,
    :government "Communist state",
    :government_desc 0,
    :capital "Beijing",
    :member_g8 false,
    :article "http //en.wikipedia.org/wiki/China"
    }],
  )
(defonce isc-grid (atom nil) )



(defn isomorphic2 []
  (let [props (clj->js {:id "countryList"
                        :width 500 :height 224
                        :alternateRecordStyles true
                        :data country-data
                        :position "relative"
                        :canReorderFields true
                        :fields [{:name "countryCode" :title "Flag" :width 50 :type "image" :imageURLPrefix "flags/16/" :imageURLSuffix ".png"},
                                 {:name "countryName" :title "Country"},
                                 {:name "capital" :title "Capital" :showIf "true"},
                                 {:name "continent" :title "Continent"}
                                 ]
                        })
        _ (js/console.log props)
        grid (js/isc.ListGrid.create  props)
        el (js/document.getElementById "my-id")]
    ;; (js/console.log grid)
    (.setHtmlElement grid el)
    (.redraw grid)
    (reset! isc-grid grid)
    ))


(defn isomorphic []
  ;; isc.Canvas.resizeFonts(3);
  ;; isc.Canvas.resizeControls(10);
  (let [button (goog/getValueByKeys js/isc "Button" "create")]
    (js/console.log "BUTTON" button)

    (let [button (js/isc.Button.create  #js{:title "Hello"
                                            :position "relative"
                                            :width 150
                                            :click #(js/console.log "Hello")})
          el (js/document.getElementById "my-id")]
      (js/console.log button)
      (.setHtmlElement button el)
      (.redraw button)
      ))
  )


    ;; (s/button {:primary true :circular true
    ;;            :onClick (fn []
    ;;                       (println "Hello world")
    ;;                       (let [{:keys [grid-api]} state
    ;;                             selected-nodes (.getSelectedNodes grid-api)]
    ;;                         (println grid-api)
    ;;                         (js/console.log selected-nodes)
    ;;                         )

    ;;                       )} "Click me!!!")


;; (def column-defs (make-column-defs ["make" "model" "price"]))
;; (def row-data [{:make "Toyota" :model "Celica" :price 35000 },
;;                {:make "Ford" :model "Mondeo" :price 32000 },
;;                {:make "Porsche" :model "Boxter" :price 72000 }])

;; (defn react-data-grid [props]
;;   (let [props (clj->js props)
;;         create-element-fn (.-createElement react)]
;;     ;; (js/console.log (apply create-element-fn react-data-grid-class props nil))
;;     (apply create-element-fn react-data-grid-class props nil)))
