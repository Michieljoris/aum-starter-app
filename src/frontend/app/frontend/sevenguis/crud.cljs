(ns app.frontend.sevenguis.crud
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [goog.object :as goog]
   [pagora.aum.frontend.util :refer [make-cmp]]
   [cuerdas.core :as str]
   [pagora.aum.modules.semantic.core :as s]))

(defn make-crud-list-item [this crud-selection {:keys [id first-name surname]}]
  (s/list-item {:style {:color "#000000de"}
                :onClick (fn []
                           (om/update-state! this assoc :selected-id id)
                           (om/update-state! this assoc :first-name first-name)
                           (om/update-state! this assoc :surname surname))
                :active (= crud-selection id)} (str surname ", " first-name)))

(defui ^:once Crud
  Object
  (initLocalState [this]
    {:crud-list (array-map 1 {:id 1 :first-name "Hans" :surname "Emil"}
                           2 {:id 2 :first-name "Max" :surname "Musterman"})
     :next-id 3})
  (render [this]
    (let [{:keys [selected-id crud-list first-name
                  surname next-id filter-str]} (om/get-state this)]
      (html
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
                   "Delete")]]))))

(def crud (make-cmp Crud))
