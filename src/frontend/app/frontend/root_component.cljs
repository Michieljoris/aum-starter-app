(ns app.frontend.root-component
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [pagora.aum.frontend.util :refer [make-cmp om-data]]
   [pagora.aum.modules.semantic.core :as s]

   [app.frontend.sevenguis.counter :refer [counter]]
   [app.frontend.sevenguis.temperature-converter :refer [temperature-converter]]
   [app.frontend.sevenguis.flight-booker :refer [flight-booker]]
   [app.frontend.sevenguis.timer :refer [timer]]
   [app.frontend.sevenguis.crud :refer [crud]]
   [app.frontend.sevenguis.circle-drawer :refer [circle-drawer]]
   [app.frontend.sevenguis.cells :refer [Cells cells]]))

(defn make-menu-item [this selection label key]
  (s/menu-item {:name label :active (= selection key)
                :onClick (fn [_]
                            (om/update-state! this assoc :gui key) ;;for the menu cmp
                           ;; Fiddle with the dom for the quick and dirty menu
                            (let [old-el (js/document.getElementById (name selection))
                                  current-el (js/document.getElementById (name key))]
                              (set! (.. old-el -style -display) "none")
                              (set! (.. current-el -style -display) "block"))
                           ;; We need to update the timer cmp to make sure it
                           ;; only runs when it's selected
                           (om/transact! this [`(sevenguis/select {:gui ~key}) [:cmp :timer]]))}))

;;Quick and dirty menu
(def menu-items
  (partition 4 ["Counter" :counter counter {:place :holder}
                "Temperature Converter" :temperature-converter temperature-converter  {:place :holder}
                "Flight Booker" :flight-booker flight-booker {:place :holder}
                "Timer" :timer timer {:place :holder}
                "CRUD" :crud crud {:place :holder}
                "Circle Drawer" :circle-drawer circle-drawer {:place :holder}
                "Cells" :cells cells :cells-data]))

(defui ^:once Menu
  static om/Ident
  (ident [this props]
    [:cmp :Menu])
  static om/IQuery
  (query [this] [:client/gui])
  Object
  (initLocalState [this]
    {:gui :counter}) ;;default gui
  (componentDidMount [this]
    (let [{:keys [gui]} (om/get-state this)]
      (doseq [[_ key _ _] menu-items]
        (when (not= key gui)
          (when-let [el (js/document.getElementById (name key))]
            (set! (.. el -style -display) "none"))))))
  (render [this]
    (let [{:keys [gui]} (om/get-state this)]
      (html
       (apply s/menu {:fluid true :vertical true :tabular true}
              (map (fn [[label key _ _]]
                     (make-menu-item this gui label key))
                   menu-items))))))

(def menu (make-cmp Menu))

(defui ^:once RootComponent
  static om/IQuery
  (query [this] [:client/reload-key
                 {:cells-data (om/get-query Cells)}])
  Object
  (render [this]
    (html
     (s/container
      (s/grid {:style {:paddingTop 30}}
        ;;Menu
        (s/row
            (s/column {:width 4}
                (menu this {:place :holder}))
          ;;Gui
          (s/column {:width 12 :stretched false}
              (html
               (apply conj [:div]
                      (map (fn [[_ _ f d]] (f this d))
                           menu-items))))))))))

