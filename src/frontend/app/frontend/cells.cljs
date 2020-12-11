(ns app.frontend.cells
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [pagora.aum.frontend.util :refer [make-cmp om-data]]
   [goog.object :as goog]
   [cuerdas.core :as str]
   [app.frontend.compute-cells :refer [parse-formula Emptie refs evaluate cell-str alphabet]]
   [app.frontend.semantic :as s]))

(def cells-dimensions {:rows 100 :columns 26
                       :view-port-size 600
                       :grid-size 2500})
;; (def cells-dimensions {:rows 4 :columns 4
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
           {:keys [active?]} :state} (om-data this)
          {:keys [grid-size columns]} cells-dimensions]

      (s/table-cell
       {:id (+ r c)
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
  static om/IQuery
  (query [this] [{:cells (om/get-query TableCell)}])
  Object
  (render [this]
    (let [{{:keys [cells]}:props} (om-data this)
          {:keys [view-port-size grid-size columns rows]} cells-dimensions]
      (timbre/info :#pp {:cells-query (om/get-query this)})
      (html
       [:div {:style {:width view-port-size :overflow "auto"
                      :height view-port-size}}
        [:div {:style {:width grid-size}}
         (s/table
          {:definition true :compact "very" :celled true :size "small"}
          (s/table-header {:fullWidth true}
                          (apply s/table-row
                                 (s/table-header-cell)
                                 (map
                                  #(s/table-header-cell {:textAlign "center"} %)
                                  (take (:columns cells-dimensions) (seq alphabet)))))

          (apply s/table-body
                 (map (fn [r]
                        (apply s/table-row
                               (s/table-cell {:collapsing true} r)
                               (map #(table-cell this (nth cells (+ (* r 4) %)))
                                    (range columns))))
                      (range rows))))]]))))

(def cells (make-cmp Cells))










;;============================================
;; Scratchpad

(defui ^:once Input
  static om/IQuery
  (query [this] [:value])
  Object
  (initLocalState [this]
    (:props (om-data this)))
  (render [this]
    (let [{:keys [state props]} (om-data this)]
      (s/input {:fluid true  :transparent true
                ;; :value (or (get-in (om/get-state this) [:cells [r c] :value]) "")
                :defaultValue (or (:value (om/get-state this)) "")
                :onBlur #(do

                           (let [value (goog/getValueByKeys % "target" "value")
                                 formula (parse-formula value)]
                             (timbre/info value)
                             (timbre/info formula)
                             ;; (om/update-state! this update-in [:cells [r c]]
                             ;;                   (fn [c]
                             ;;                     (timbre/info :#pp c)

                             ;;                     (assoc c formula :formula
                             ;;                            :value value)))
                             )
                           (timbre/info "blur"))
                ;; :onFocus #(update-state! assoc )
                :onChange #(let [value (goog/getValueByKeys %2 "value")]
                             (om/update-state! this assoc :value value)
                             ;; (om/update-state! this update-in [:cells [r c]]
                             ;;                   assoc :value value
                             ;;                   )
                             )
                }))
    )
  )
(def input (make-cmp Input))

      ;; ;; (js/console.log "formula:" (to-str (get-in (om/get-state this) [:cells [0 0] :formula])))
      ;; (let [formula (parse-formula "123")
      ;;       ;; formula (parse-formula "=A1")
      ;;       ]
      ;;   ;; (js/console.log formula)
      ;;   ;; (js/console.log (cell-str {:formula formula :value (evaluate formula, cells) ;; :type  (type formula)
      ;;   ;;                            }))
      ;; ;;   ;; (if (= Textual (type formula))
      ;; ;;   ;;   (to-str formula)
      ;; ;;   ;;   (str value))
      ;; ;;   ;; (js/console.log "????" (evaluate formula cells))
      ;; ;;   ;; (js/console.log (parse-formula "=A1:A2"))
      ;; ;;   ;; (js/console.log "eval:" (evaluate formula cells))
      ;; ;;   ;; (js/console.log "refs:" (refs formula cells))
      ;; ;;   ;; (js/console.log "to-str:" (to-str formula))
      ;; ;;   ;; (js/console.log (parse-formula "123.1"))
      ;;   )



;; (defui ^:once Cell
;;   static om/Ident
;;   (ident [this {:keys [r c]}]
;;     [:cells [r c]])
;;   static om/IQuery
;;   (query [this] [:r :c :value :formula])
;;   Object
;;   (render [this]
;;     (let [{{:keys [r c value formula] :as cell} :props
;;            {:keys [active?]} :state} (om-data this)]
;;       (timbre/info "in Cell")

;;       (timbre/info :#pp cell)

;;       (html [:div "in cell"
;;              ])
;;    )))


;; (def cell (make-cmp Cell))


;; ==================================================

(defn change-prop [this {:keys [r c value formula observers]}]
  (let [cells (:cells (om/get-state this))
        new-value  (evaluate formula cells)]
    (when-not (or (= value new-value) (and (js/isNaN value) (js/isNaN new-value)))
      (om/update-state! this assoc-in [:cells [r c] :value] new-value)
      ;; (doseq [[r c] observers] (change-prop this (cell-at this r c)))
      )))

(defn update-cell-old [this event r c cells]
  (let [content (or (goog/getValueByKeys event "target" "value") "")
        formula (if (empty? content)
                  Emptie
                  (parse-formula content))
        oldform (:formula (cells [r c]))]
    (doseq [cell (refs oldform cells)]
      (om/update-state! this update-in [:cells [(:r cell) (:c cell)] :observers]
                        (fn [obs] (remove #(= % [r c]) obs))))
    (doseq [cell (refs formula cells)]
      (om/update-state! this update-in [:cells [(:r cell) (:c cell)] :observers]
             #(conj % [r c])))

    (om/update-state! this update-in [:cells [r c]] assoc :formula formula :content content)
    ;; (change-prop this (cell-at this r c))
    ))
