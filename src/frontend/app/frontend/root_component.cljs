(ns app.frontend.root-component
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [pagora.aum.frontend.util :refer [make-cmp om-data]]
   [goog.object :as goog]
   [cuerdas.core :as str]
   [js.react :as react]
   ;; [js.ag-grid-react :as ag-grid-class]
   ;; [js.react-data-grid :as react-data-grid-class]
   ;; [pagora.aum.modules.semantic.core :as s]
   [app.frontend.semantic :as s]
   [app.frontend.circle-drawer :refer [circle-drawer]]
   [app.frontend.cells :refer [cells]]
   ))

;;TODO:
;; Put semantic back in aum
;; Temperature: When the user enters a non-numerical string into TC the value in
;; TF is not updated and vice versa.
;; get state not via om-data, overkill without using computed and props


(defn counter [this _]
  (let [{{:keys [counter]} :state} (om-data this)]
    [:div (s/button {:basic true
                     :style {:marginRight 10}
                     :onClick #(om/update-state! this update :counter inc)}
                    "Count")
     counter]))


(defn fahrenheit->celsius [fahrenheit]
  (* (- fahrenheit 32) (/ 5 9)))

(defn celsius->fahrenheit [celsius]
  (+ (* celsius (/ 9 5)) 32))

(defn temperature-input [this type other-type conversion-fn]
  (let [{:keys [state]} (om-data this)
        label (str/capital (name type))]
    [:div {:class "ui right labeled input"}
     [:input {:type "number"
              :style {:width 60}
              :value (if-let [v (get state type)]
                       (js/Math.round v) "")
              :onChange #(let [value (goog/getValueByKeys % "target" "value")]
                          (om/update-state! this assoc type value)
                          (om/update-state! this assoc other-type (conversion-fn value)))}]
     [:div {:class "ui basic label"} label]]))

(defn temperature-converter [this _]
  [:div
   (temperature-input this :celsius :fahrenheit celsius->fahrenheit)
   [:span {:class "pad-lef-5 pad-rig-5"} "="]
   (temperature-input this :fahrenheit :celsius fahrenheit->celsius)])

(defn date? [m]
  (.isValid m) )

(def flight-options
  [{:text "One way flight"
     :value :one-way-flight}
   {:text "Return flight"
    :value :return-flight}])

(defn enter-date [this date-type {:keys [error? disabled?]}]
  (let [{:keys [state]} (om-data this)
        date (state date-type)]
    [:div {:class (cond-> "ui left labeled input mar-top-10"
                    error? (str " error"))}
     [:input {:type "text" :value (or date "")
              :style {:width 110}
              :disabled disabled?
              :onChange #(let [value (goog/getValueByKeys % "target" "value")]
                           (om/update-state! this assoc date-type value))} ]]))

(defn flight-booker [this _]
  (let [{{:keys [flight-type leave-date return-date]} :state} (om-data this)
        leave-moment (js/moment leave-date "DD.MM.YYYY" true)
        return-moment (js/moment return-date "DD.MM.YYYY" true)
        valid-leave-date? (date? leave-moment)
        valid-return-date? (date? return-moment)]
    [:div
     ;; Select flight type
     (s/dropdown {:inline true
                  :options flight-options
                  :onChange  #(om/update-state! this assoc
                                                :flight-type (keyword (goog/getValueByKeys %2 "value")))
                  :value flight-type}) [:br]

     ;; Enter leave date
     (enter-date this  :leave-date {:error? (not valid-leave-date?)
                                    :disabled? false}) [:br]

     ;; Enter return date
     (enter-date this  :return-date {:error? (not valid-return-date?)
                                     :disabled? (= flight-type :one-way-flight)}) [:br]

     ;; Book flight
     (s/button {:basic true
                :style {:marginTop 10}
                :disabled (case flight-type
                            :one-way-flight (not valid-leave-date?)
                            :return-flight (or (not valid-leave-date?) (not valid-return-date?)
                                               (.isSameOrAfter leave-moment return-moment)))
                :onClick #(om/update-state! this assoc :flight-booker-modal-open? true)}
               "Book")
    
     ;; Dialog
     (s/modal
      {:onClose #(om/update-state! this assoc :flight-booker-modal-open? false)
       :size "tiny"
       :open (:flight-booker-modal-open? (om/get-state this))
       :content (case flight-type
                  :one-way-flight (str "You booked a one-way flight on " leave-date)
                  :return-flight (str "You booked a return flight on " leave-date " and " return-date))
       :actions [{:key "ok", :content "OK", :positive true }]})]))

(defn timer [this _]
  (let [{{:keys [tick ticker max-tick]} :state} (om-data this)]
    ;;TODO: turn off ticker in other guis
    (when (not ticker)
      (om/update-state! this assoc :ticker
                        (js/setInterval #(let [{{:keys [tick max-tick]} :state} (om-data this)]
                                          (when (< tick max-tick)
                                            (om/update-state! this update :tick inc))) 100)))
    
    [:div
     [:div "Elapsed time:"]
     (s/progress {:style {:marginBottom 20 :minWidth 0}
                  :value tick
                  :total max-tick})
     (str (/ tick 10) "s")
     [:br]

     [:div {:class "ui input mar-top-10"
            :style {:width "100%"}}
      [:span {:class "mar-rig-10"} "Duration: "]
      [:input {:type "range"
               :min 1
               :max 400
               :value max-tick
               :style {:padding 0}
               :onChange #(let [value (goog/getValueByKeys % "target" "value")]
                            (om/update-state! this assoc :max-tick value))} ]]
     [:br]
     (s/button {:style {:marginTop "10px"}
                :basic true
                :onClick #(om/update-state! this assoc :tick 0)}
               "Reset")]))

(defn make-crud-list-item [this crud-selection {:keys [id first-name surname]}]
  (s/list-item {:style {:color "#000000de"}
                :onClick (fn []
                           (om/update-state! this assoc :selected-id id)
                           (om/update-state! this assoc :first-name first-name)
                           (om/update-state! this assoc :surname surname))
                :active (= crud-selection id)} (str surname ", " first-name)))

(defn crud [this _]
  (let [{{:keys [selected-id crud-list first-name surname next-id filter-str]} :state} (om-data this)]
    [:div
     ;; Filter
     (s/form (s/form-field {:inline true}
                           (html
                            [:label {:style {:min-width 70}} "Filter prefix"])
                           (s/input {:onChange #(let [value (goog/getValueByKeys % "target" "value")]
                                                  (om/update-state! this assoc :filter-str value))}))) [:br]
     (s/grid
         (s/row
             ;; List of persons on the left
             (s/column {:width 10}
                 (s/segment {:style {:height 300
                                     :overflowX "auto"
                                     :overflowY "auto"}}
                            (apply s/list {:selection true :verticalAlign "middle"}
                                   (->> (vals crud-list)
                                        (filter #(or (str/empty-or-nil? filter-str)
                                                     (str/starts-with? (:surname %) filter-str)))
                                        (map (partial make-crud-list-item this selected-id))))))
           ;; First and last name inputs on the right
           (s/column {:width 6}
               (s/form (s/form-field {:inline true}
                                     (html
                                      [:label {:style {:min-width 70}} "First name"])
                                     (s/input {:value (or first-name "")
                                               :onChange #(let [value (goog/getValueByKeys % "target" "value")]
                                                            (om/update-state! this assoc :first-name value))}))
                       (s/form-field {:inline true}
                                     (html
                                      [:label {:style {:min-width 70}} "Surname"])
                                     (s/input {:value (or surname "")
                                               :onChange #(let [value (goog/getValueByKeys % "target" "value")]
                                                            (om/update-state! this assoc :surname value))})))))) [:br]

     ;; Cud buttons
     [:div
      (s/button {:basic true
                 :onClick  #(do
                              (om/update-state! this update :crud-list assoc
                                                next-id {:id next-id :first-name first-name :surname surname})
                              (om/update-state! this update :next-id inc))}
                "Create")
      (s/button {:basic true
                 :onClick  #(om/update-state! this update-in [:crud-list selected-id] merge
                                              {:first-name first-name :surname surname})}
                "Update")
      (s/button {:basic true
                 :onClick  #(om/update-state! this update :crud-list dissoc selected-id)}
                "Delete")]]))

(defn nop [this] "not implemented")

(defn make-menu-item [this selection name item]
  (s/menu-item {:name name :active (= selection item)
                :id item
                :onClick #(do
                           (js/clearInterval (:ticker (om/get-state this)))
                           (om/update-state! this assoc :menu-selection item :ticker nil))}))

(def menu-items
  (partition 3 ["Counter" :counter counter 
                "Temperature Converter" :temperature-converter temperature-converter 
                "Flight Booker" :flight-booker flight-booker
                "Timer" :timer timer
                "CRUD" :crud crud
                "Circle Drawer" :circle-drawer circle-drawer
                "Cells" :cells cells]))

(def actions
  (into {} (map (fn [[_ k f]] [k f]) menu-items)))

(defui ^:once RootComponent
  static om/IQuery
  (query [this] [:client/reload-key])
  Object
  (initLocalState [this]
    {:menu-selection :cells ;;default menu selection
     :counter 0
     :tick 0 :max-tick 100
     :flight-type :one-way-flight
     :leave-date "24.12.2020"
     :return-date "24.12.2020"
     :crud-list (array-map 1 {:id 1 :first-name "Hans" :surname "Emil"}
                           2 {:id 2 :first-name "Max" :surname "Musterman"})
     :next-id 3
     })
  (render [this]
    (let [{{:keys [menu-selection]} :state} (om-data this)]
      (html
       (s/container
        (s/grid {:style {:paddingTop 30}}
          (s/row
              (s/column {:width 4}
                  (apply s/menu {:fluid true :vertical true :tabular true}
                         (map (fn [[name key _]]
                                (make-menu-item this menu-selection name key))
                              menu-items)))
              (s/column {:width 12 :stretched false}
                  (html
                   ((actions menu-selection) this {})))))
        )))))

;; (def menu (make-cmp Menu))

;; (defn ag-grid [props]
;;   (let [props (clj->js props)
;;         create-element-fn (.-createElement react)]
;;     (create-element-fn ag-grid-class props)))

;; (defn make-column-defs [props]
;;   (->> props
;;        (mapv (fn [prop]
;;                {:headerName (str/capital (name prop)) :field (name prop)
;;                 :sortable true :filter true ;; :checkboxSelection true
;;                 }))))

;; (def queries {:account [:id :name]
;;               :user [:id :name :email]
;;               :role [:id :name]
;;               :subscription [:id]})

;; (defn table [this {:keys [rows table auth-tables-state]}]
;;   (let [columns (get queries table)
;;         on-selected (fn [ args]
;;                       (let [{:strs [api]}(js->clj args)
;;                             selected-rows (js->clj (.getSelectedRows api))
;;                             selected-id (get-in selected-rows [0 "id"])
;;                             read-record-kws (->> (dissoc auth-tables-state table)
;;                                                  (filter (fn [[_ {:keys [tables]}]]
;;                                                            (contains? tables table)))
;;                                                  keys
;;                                                  (mapv #(keyword (str (name %) "-records"))))]

;;                         (om/transact! this (into [`(aum/set-selected-auth-id {:table ~table
;;                                                                               :id ~selected-id})]
;;                                                    read-record-kws))))]
;;     (html
;;      [:div {:class "ag-theme-balham"
;;             :style {:height 250
;;                     :border "black solid 1px"
;;                     ;; :width 600
;;                     }}
;;       (ag-grid {:columnDefs (make-column-defs columns)
;;                 :rowData (if (empty? rows) [] rows)
;;                 :onSelectionChanged on-selected
;;                 :rowDeselection true
;;                 :onGridReady (fn [params]
;;                                (om/update-state! this assoc :grid-api (.-api params)))
;;                 :rowSelection "single"})]))
;;   )

;; (defn set-table-constraints [this table selected-table args]
;;   (let [{:strs [checked]} (js->clj args)]
;;     (om/transact! this [`(aum/set-table-constraints{:table ~table
;;                                                     :selected-table ~selected-table
;;                                                     :checked ~checked})
;;                         (keyword (str (name table) "-records"))])))

;; (defui ^:once RootComponent
;;   static om/IQuery
;;   (query [this] [:client/reload-key]
;;     ;; [:client/reload-key
;;     ;;  {:account-records (get queries :account)}
;;     ;;  {:user-records (get queries :user)}
;;     ;;  {:role-records (get queries :role)}
;;     ;;  {:subscription-records (get queries :subscription)}
;;     ;;  :client/auth-tables-state
;;     ;;  ]
;;     )
;;   Object
;;   ;; (componentDidMount [this]
;;   ;;   ;; (isomorphic2)
;;   ;;   ;; (om/update-state! assoc :grid grid)
;;   ;;   )
;;   (render [this]
;;     (html
;;      [:div
;;       (menu this {:foo 1})
;;       ;; (let [{:keys [props state computed] :as data} (om-data this)
;;       ;;       {:keys [:account-records :user-records :role-records :subscription-records
;;       ;;               client/auth-tables-state]} props]
;;       ;;   ;; (timbre/info :#pp {:data data ;; :query (om/get-query this)
;;       ;;   ;;                    })
;;       ;;   (timbre/info :#pp {:PROPS props})


;;       ;;   (html [:div {:class "level"}
;;       ;;          [:div {:class "level-item has-text-centered"}
;;       ;;           ;; s/container {:fluid false}
;;       ;;           [:div {:style {:margin "10px 100px 10px 100px"}}
;;       ;;            "Accounts"
;;       ;;            (s/checkbox {:label "Selected user:"
;;       ;;                         :onClick #(set-table-constraints this :account :user %2)})
;;       ;;            (s/checkbox {:label "Selected role:"
;;       ;;                         :onClick #(set-table-constraints this :account :role %2)})
;;       ;;            (table this {:rows account-records  :table :account :auth-tables-state auth-tables-state})
;;       ;;            "Users"
;;       ;;            "   Filter on:"
;;       ;;            (s/checkbox {:label "Selected account:"
;;       ;;                         :onClick #(set-table-constraints this :user :account %2)})
;;       ;;            (s/checkbox {:label "Selected role:"
;;       ;;                         :onClick #(set-table-constraints this :user :role %2)})
;;       ;;            (table this {:rows user-records :table :user :auth-tables-state auth-tables-state})
;;       ;;            "Roles"
;;       ;;            (s/checkbox {:label "Selected account:"
;;       ;;                         :onClick #(set-table-constraints this :role :account %2)})
;;       ;;            (s/checkbox {:label "Selected user:"
;;       ;;                         :onClick #(set-table-constraints this :role :user %2)})
;;       ;;            (table this {:rows role-records :table :role :auth-tables-state auth-tables-state})
;;       ;;            "Subscriptions"
;;       ;;            (s/checkbox {:label "Selected account:"})
;;       ;;            (s/checkbox {:label "Selected user:"})
;;       ;;            (table this {:rows subscription-records :table :subscription :auth-tables-state auth-tables-state})]

;;       ;;            ;; (container
;;       ;;            ;;  (grid {:columns 3 :divided true}
;;       ;;            ;;    (row {}
;;       ;;            ;;      (column {}
;;       ;;            ;;        (str "Aum starter app " (-> props :user first :name)))
;;       ;;            ;;      (column
;;       ;;            ;;          (button {:primary true :circular true  :onClick #(println "Hello world")} "Click me!!!")))
;;       ;;            ;;    (row
;;       ;;            ;;        ;; (ag-grid)
;;       ;;            ;;        (column (str "Aum starter app " (-> props :user first :name)))
;;       ;;            ;;      (column
;;       ;;            ;;          (checkbox ;; {:primary true :circular true  :onClick #(println "Hello world")}
;;       ;;            ;;           "Click me!!!")))))
;;       ;;            ]]))
;;       ])
;;     ))


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




;; (def column-defs (make-column-defs ["make" "model" "price"]))
;; (def row-data [{:make "Toyota" :model "Celica" :price 35000 },
;;                {:make "Ford" :model "Mondeo" :price 32000 },
;;                {:make "Porsche" :model "Boxter" :price 72000 }])

;; (defn react-data-grid [props]
;;   (let [props (clj->js props)
;;         create-element-fn (.-createElement react)]
;;     ;; (js/console.log (apply create-element-fn react-data-grid-class props nil))
;;     (apply create-element-fn react-data-grid-class props nil)))
