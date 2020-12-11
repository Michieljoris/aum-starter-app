(ns app.frontend.mutations
 (:require
  [taoensso.timbre :as timbre]
  [pagora.aum.om.next :refer [ref->any]]
  [pagora.aum.frontend.parser.mutate :refer [mutate]]
  [app.frontend.compute-cells :refer [Emptie parse-formula refs evaluate]])
  )
(defn cell-at [state cell]
  (get-in @state [:cells/by-rc cell]))

(defn change-prop [state {:keys [r c value formula observers]}]
  (let [cells (:cells/by-rc @state)
        new-value  (evaluate formula cells)]
    (when-not (or (= value new-value) (and (js/isNaN value) (js/isNaN new-value)))
      (swap! state assoc-in [:cells/by-rc [r c] :value] new-value)
      (doseq [[r c] observers] (change-prop state (cell-at state [r c]))))))

(defn update-cell [state cell content]
  (let [{cells :cells/by-rc} @state
        formula (if (empty? content)
                  Emptie
                  (parse-formula content))
        oldform (:formula (cells cell))]
    (doseq [{:keys [r c]} (refs oldform cells)]
      (swap! state update-in [:cells/by-rc [r c] :observers]
                        (fn [obs] (remove #(= % cell) obs))))
    (doseq [{:keys [r c]} (refs formula cells)]
      (swap! state update-in [:cells/by-rc [r c] :observers]
             #(conj % cell)))

    (swap! state update-in [:cells/by-rc cell] assoc :formula formula :content content)
    (change-prop state (cell-at state cell))))


(defmethod mutate 'cells/update
  [{:keys [state reconciler component] :as env} _ {:keys [cell content]}]
  {:action (fn []
             (update-cell state cell content))})
