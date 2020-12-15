(ns app.frontend.sevenguis.cells
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [pagora.aum.frontend.util :refer [make-cmp om-data]]
   [goog.object :as goog]
   [app.frontend.cells-grammar :refer [cell-str alphabet]]
   [pagora.aum.modules.semantic.core :as s]))

(def cells-dimensions {:rows 100 :columns 26
                       :view-port-size 600
                       :grid-size 2500})

;; (def cells-dimensions {:rows 2 :columns 2
;;                        :view-port-size 600
;;                        :grid-size 600})

(defn update-cell [this event {:keys [r c] :as cell}]
  (let [app-state (deref (om/app-state (om/get-reconciler this)))
        get-dependents (fn collect-dependents [{:keys [observers]}]
                         (apply conj observers (mapcat collect-dependents
                                                       (map #(get-in app-state (conj [:cells/by-rc] %)) observers))))

        dependents (get-dependents (get-in app-state [:cells/by-rc [r c]]))
        dependents (map #(conj [:cells/by-rc] %) dependents)
        content (or (goog/getValueByKeys event "target" "value") "")
        mutation `(cells/update {:cell ~[r c] :content ~content})]
    (om/update-state! this assoc :active? nil)
    (om/transact! this (apply conj [mutation] dependents))))

(defui ^:once TableCell
  static om/Ident
  (ident [this {:keys [r c]}]
    [:cells/by-rc [r c]])
  static om/IQuery
  (query [this] [:r :c :value :formula :content :observers])
  Object
  (render [this]
    (let [{{:keys [r c content] :as cell} :props
           {:keys [active?]} :state} (om-data this)]

       (s/table-cell
        {:id (str r "-" c)
        :style {:maxWidth 0}
        :onClick #(when (not active?)
                   (om/update-state! this assoc :active? nil))
        :onDoubleClick #(om/update-state! this assoc :active? true)}

       (html
        (if active?
          (s/input {:fluid true  :transparent true :autoFocus true
                    :defaultValue content
                    :onKeyPress #(let [char-code (goog/get % "which")]
                                   (when (= (char char-code) \return)
                                     (update-cell this % cell)))
                    :onBlur #(update-cell this % cell)})
          [:div {:style {:overflow "hidden"}}
           [:span (cell-str cell)]]))))))

(def table-cell (make-cmp TableCell))

(defui ^:once Cells
  static om/Ident
  (ident [this props]
    [:cmp :cells])
  static om/IQuery
  (query [this] [{:cells (om/get-query TableCell)}])
  Object
  (render [this]
    (let [{:keys [cells]} (om/props this)
          {:keys [view-port-size grid-size columns rows]} cells-dimensions]
      (html
       [:div#cells {:style {:width view-port-size :overflow "auto"
                      :height view-port-size}}
        [:div {:style {:width grid-size}}
         (s/table
          {:definition true :compact "very" :celled true :size "small"}
          (s/table-header {:fullWidth true}
                          (apply s/table-row
                                 (s/table-header-cell)
                                 (map
                                  #(s/table-header-cell {:textAlign "center"} %)
                                  (take columns (seq alphabet)))))
          (apply s/table-body
                 (map (fn [r]
                        (apply s/table-row
                               (s/table-cell {:collapsing true} r)
                               (map #(table-cell this (nth cells (+ (* r columns) %)))
                                (range columns))))
                      (range rows))))]]))))

(def cells (make-cmp Cells))
