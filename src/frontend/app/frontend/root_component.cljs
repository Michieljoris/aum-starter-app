(ns app.frontend.root-component
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [pagora.aum.frontend.util :refer [make-cmp om-data]]
   [js.semantic-ui-react :as semantic-ui]
   [goog.object :as goog]
   [js.react :as react]
   [js.ag-grid-react :as ag-grid-class]
   [js.react-data-grid :as react-data-grid-class]
   ))

;; (js/console.log react-data-grid-class)

(defn component
  "Get a component from sematic-ui-react:
    (component \"Button\")
    (component \"Menu\" \"Item\")"
  [k & ks]
  (let [cmp (if (seq ks)
              (apply goog/getValueByKeys semantic-ui k ks)
              (goog/get semantic-ui k))]
    (fn [& args]
      (let [[props & children] args
            [props children] (if (map? props)
                               [props children] [{} args])]
        (apply (.-createElement react) cmp (clj->js props) children)))))

(def container      (component "Container"))
(def grid (component "Grid"))
(def column (component "Grid" "Column"))
(def row (component "Grid" "Row"))
(def button (component "Button"))
(def checkbox (component "Checkbox"))
(def confirm (component "Confirm"))

(def segment        (component "Segment"))
(def dimmer         (component "Dimmer"))
(def loader         (component "Loader"))
(def message        (component "Message"))
(def message-header (component "Message" "Header"))

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

(defn react-data-grid [props]
  (let [props (clj->js props)
        create-element-fn (.-createElement react)]
    ;; (js/console.log (apply create-element-fn react-data-grid-class props nil))
    (apply create-element-fn react-data-grid-class props nil)))


(def column-defs [{:headerName "Make" :field "make" :sortable true :filter true :checkboxSelection true},
                  {:headerName "Model" :field "model" },
                  {:headerName "Price"  :field "price" }])
(def row-data [{:make "Toyota" :model "Celica" :price 35000 },
               {:make "Ford" :model "Mondeo" :price 32000 },
               {:make "Porsche" :model "Boxter" :price 72000 }])

(defn ag-grid-demo [this state]
  (html
   [:div {:class "ag-theme-balham"
          :style {:height 250
                  :border "black solid 1px"
                  :width 600}}
    (button {:primary true :circular true
             :onClick (fn []
                        (println "Hello world")
                        (let [{:keys [grid-api]} state
                              selected-nodes (.getSelectedNodes grid-api)]
                          (println grid-api)
                          (js/console.log selected-nodes)
                          )

                        )} "Click me!!!")
    (ag-grid {:columnDefs column-defs :rowData row-data
              :onGridReady (fn [params]
                             (om/update-state! this assoc :grid-api (.-api params))
                             (js/console.log "grid api" (.-api params)))
              :rowSelection "multiple"})])
  )

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


(defui ^:once RootComponent
  static om/IQuery
  (query [this]
    [:client/reload-key
     {:user [:id :name]}
     ])
  Object
  (componentDidMount [this]
    (timbre/info :#pp (om/props this))

    (isomorphic2)
    ;; (om/update-state! assoc :grid grid)
    )
  (render [this]
    (html
    (container
     (html
      [:div
       [:div {:id "my-id"}
        (button {:primary true :circular true  :onClick (fn [] (println "Hello world")
                                                          (js/console.log @isc-grid)
                                                          (.destroy @isc-grid)
                                                          (isomorphic2)
                                                          ;; (.setData @isc-grid (clj->js []))
                                                          )} "Click me!!!")
        ;; (js/console.log (om/get-state this))
        ;; (.redraw (:grid (om/get-state this)))

        ]
       (let [{:keys [props state computed] :as data} (om-data this)]
         (timbre/info :#pp {:data data})
         (html [:div {:class "level"}
                [:div {:class "level-item has-text-centered"}
                 (container
                  "hello"

                  (ag-grid-demo this state)
                  ;; (react-data-grid {:columns columns
                  ;;                   :rowsCount 3
                  ;;                   ;; :minHeight "150"
                  ;;                   :rowGetter (fn [i]
                  ;;                                (js/console.log "Hello!!!!!!!!!!!!!!!!!!!!!!!!!!!!1" i)
                  ;;                                (clj->js (get rows i)))})

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
                  )]]))])))
    ))
