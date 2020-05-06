(ns app.frontend.root-component
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [pagora.aum.frontend.util :refer [make-cmp om-data]]
   [js.semantic-ui-react :as semantic-ui]
   [goog.object :as goog]
   [js.react :as react]
   [js.ag-grid-react :as ag-grid]
   )
  )


;; class App extends Component {
;;   constructor(props) {
;;     super(props);
;;     this.state = {
;;       columnDefs: [
;;         { headerName: "Make", field: "make" },
;;         { headerName: "Model", field: "model" },
;;         { headerName: "Price", field: "price" }],
;;       rowData: [
;;         { make: "Toyota", model: "Celica", price: 35000 },
;;         { make: "Ford", model: "Mondeo", price: 32000 },
;;         { make: "Porsche", model: "Boxter", price: 72000 }]
;;     }
;;   }

;;   render() {
;;     return (
;;       <div className="ag-theme-alpine" style={ {height: '200px', width: '600px'} }>
;;         <AgGridReact
;;             columnDefs={this.state.columnDefs}
;;             rowData={this.state.rowData}>
;;         </AgGridReact>
;;       </div>
;;     );
;;   }
;; }
(js/console.log ag-grid)

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

(js/console.log button)

(defui ^:once Foo
  static om/IQuery
  (query [this]
    [:client/foo])
  Object
  (render [this]
    (html [:div "in foo4"])))

(def foo (make-cmp Foo))


(def column-defs [{:headerName "Make" :field "make" },
                  {:headerName "Model" :field "model" },
                  {:headerName "Price"  :field "price" }])
(def row-data [{:make "Toyota" :model "Celica" :price 35000 },
               {:make "Ford" :model "Mondeo" :price 32000 },
               {:make "Porsche" :model "Boxter" :price 72000 }])

(defn ag-grid-cmp  [props]
  ((.-createElement react) ag-grid (clj->js props) {}))

(js/console.log "HJELLLLLLLLOOOOOOOOOOOOOOOOOOO")
(js/console.log (clj->js {:columDefs column-defs :rowData row-data}))

(defui ^:once RootComponent
  static om/IQuery
  (query [this]
    [:client/reload-key
     {:user [:id :name]}
     ])
  Object
  (render [this]
    (let [{:keys [props state computed] :as data} (om-data this)]
      (timbre/info :#pp {:data data})
      (html [:div {:class "level"}
             [:div {:class "level-item has-text-centered"}
              "hello to"
              [:div {:class "ag-theme-alpine"
                     :style {:height 250
                             :border "black solid 1px"
                             :width 600}}
               (ag-grid-cmp {:columDefs column-defs :rowData row-data})]

;;             columnDefs={this.state.columnDefs}
;;             rowData={this.state.rowData}>
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
              ]]))))
