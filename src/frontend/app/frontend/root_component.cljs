(ns app.frontend.root-component
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [pagora.aum.modules.semantic.core :as s]

   [app.frontend.sevenguis.counter :refer [counter]]
   [app.frontend.sevenguis.temperature-converter :refer [temperature-converter]]
   [app.frontend.sevenguis.flight-booker :refer [flight-booker]]
   [app.frontend.sevenguis.timer :refer [timer]]
   [app.frontend.sevenguis.crud :refer [crud]]
   [app.frontend.sevenguis.circle-drawer :refer [circle-drawer]]
   [app.frontend.sevenguis.cells :refer [Cells cells]]))

;;TODO:
;; Temperature: When the user enters a non-numerical string into TC the value in
;; TF is not updated and vice versa.

(defn nop [this] "not implemented")

(defn make-menu-item [this selection name item]
  (s/menu-item {:name name :active (= selection item)
                :id item
                :onClick #(do
                           (js/clearInterval (:ticker (om/get-state this)))
                           (om/update-state! this assoc :menu-selection item :ticker nil))}))

(def menu-items
  (partition 4 ["Counter" :counter counter {}
                "Temperature Converter" :temperature-converter temperature-converter  {}
                "Flight Booker" :flight-booker flight-booker {}
                "Timer" :timer timer {}
                "CRUD" :crud crud {}
                "Circle Drawer" :circle-drawer circle-drawer {}
                "Cells" :cells cells :cells-data
                ]))

(def actions
  (into {} (map (fn [[_ k f q]] [k [f q]]) menu-items)))

(defui ^:once RootComponent
  static om/IQuery
  (query [this] [:client/reload-key
                 {:cells-data (om/get-query Cells)}])
  Object
  (initLocalState [this]
    {:menu-selection :cells}) ;;default menu selection
  (render [this]
    (let [{:keys [menu-selection]} (om/get-state this)]
      ;; (timbre/info :#pp {:root-query!!!!! (om/get-query this)})

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
                  (let [[f q] (actions menu-selection)]
                    [:div
                     (when (not (#{:cells :circle-drawer :timer} menu-selection))
                       (f this q))
                     ;; Retain state for these components
                     [:div {:style {:display (if (= menu-selection :timer) "block" "none")}}
                      (timer this {})]
                     [:div {:style {:display (if (= menu-selection :circle-drawer) "block" "none")}}
                      (circle-drawer this {})]
                     [:div {:style {:display (if (= menu-selection :cells) "block" "none")}}
                      (cells this :cells-data)]]))))))))))
