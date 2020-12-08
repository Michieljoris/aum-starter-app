(ns app.frontend.circle-drawer
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [pagora.aum.frontend.util :refer [make-cmp om-data]]
   [goog.object :as goog]
   [cuerdas.core :as str]
   [app.frontend.semantic :as s]))


(defn circle [this {:keys [diameter filled? x y z-index]}]
  (let [radius (/ diameter 2)]
    [:div {:style {:position "absolute"
                   :top (str (- y radius) "px")
                   :left (str (- x radius) "px")
                   :height (str diameter "px")
                   :width (str diameter "px")
                   :background-color  (when filled? "#bbb")
                   :border "solid black 1px"
                   :border-radius "50%"
                   :display "inline-block"
                   :z-index z-index}}]))

(defn clip-boxes [box-size max-radius]
  (let [cover-box-size (+ box-size (* 2 max-radius))]
    [[:div {:style {:position "absolute" :width cover-box-size :height max-radius
                    :z-index 1000000 :background "white"
                    :top box-size :left (- max-radius)}}]
     [:div {:style {:position "absolute" :height cover-box-size :width max-radius
                    :z-index 1000000 :background "white"
                    :top (- max-radius) :left box-size}}]
     [:div {:style {:position "absolute" :width cover-box-size :height max-radius
                    :z-index 1000000 :background "white"
                    :top (- max-radius) :left (- max-radius)}}]
     [:div {:style {:position "absolute" :height cover-box-size :width max-radius
                    :z-index 1000000
                    :background "white"
                    :top (- max-radius) :left (- max-radius)}}]]))

(defn get-mouse-location [e]
  (let [x (goog/getValueByKeys e "nativeEvent" "clientX")
        y (goog/getValueByKeys e "nativeEvent" "clientY")
        client-rect (.getBoundingClientRect (goog/getValueByKeys e "currentTarget"))
        x (.round js/Math (- x (goog/getValueByKeys client-rect "left")))
        y (.round js/Math (- y (goog/getValueByKeys client-rect "top")))]
    [x y]))

(defn resize-circle-modal [this max-radius ]
  (let [close #(om/update-state! this assoc :resize-circle-modal-open? false)]
    (s/modal {:onClose close
              :open (:resize-circle-modal-open? (om/get-state this))
              :size "mini"}
             (s/modal-header "Adjust diameter of circle")
             (s/modal-content
              (html
               [:div {:class "ui input mar-top-10"
                      :style {:width "100%"}}
                [:input {:type "range"
                         :min 10
                         :max (* max-radius 2)
                         :value (get-in (om/get-state this) [:diameter])
                         :style {:padding 0}
                         :onChange #(let [value (goog/getValueByKeys % "target" "value")
                                          {:keys [current-nearest-circle]} (om/get-state this)]
                                      (om/update-state! this assoc :diameter value)
                                      (om/update-state! this assoc-in [:circles (:z-index current-nearest-circle)
                                                                                  :diameter] value)
                                      ;; (om/update-state! this assoc :diameter value)
                                      )} ]]))
             (s/modal-actions
              (s/button {:content "Close" :onClick close}))
             )))

(defn distance [[x1 y1] [x2 y2]]
  (.sqrt js/Math (+ (.pow js/Math (- x1 x2) 2)
                    (.pow js/Math (- y1 y2) 2)))

  )
(defn circle-drawer [this]
  (let [{{:keys [circles next-z-index show-size-menu? click-x click-y current-nearest-circle]} :state} (om-data this)
        box-size 300
        max-radius 30]
    [:div
     (resize-circle-modal this max-radius)
     (conj (apply conj [:div {:style {:position "relative" :marginLeft 20}}] (clip-boxes box-size max-radius))
           (apply conj [:div {:style {:width box-size :height box-size :border "1px solid lightgrey"}
                              :onContextMenu (fn [e]
                                               (let [{:keys [diameter] x1 :x y1 :y} current-nearest-circle
                                                     [x y] (get-mouse-location e)
                                                     in-circle? (< (distance  [x y] [x1 y1]) (/ diameter 2))]
                                                 (om/update-state! this assoc :diameter diameter
                                                                   :click-x x :click-y y :show-size-menu?
                                                                   in-circle?)
                                                 (.preventDefault e)))
                              :onMouseUp (fn [e]
                                           (when (= (goog/getValueByKeys e "nativeEvent" "button") 0)
                                             (om/update-state! this assoc :show-size-menu? false)))
                              :onMouseMove (fn [e]
                                             (let [[x y] (get-mouse-location e)
                                                   {:keys [circles]} (om/get-state this)
                                                   circles (map #(assoc % :distance (distance [x y] [(:x %) (:y %)]))
                                                                circles)
                                                   {:keys [diameter z-index] :as nearest-circle}
                                                   (apply min-key :distance circles)]
                                               (when (not= (:z-index current-nearest-circle) z-index)
                                                 (when current-nearest-circle
                                                   (om/update-state! this assoc-in [:circles (:z-index current-nearest-circle)
                                                                                    :filled?] false))
                                                 (om/update-state! this assoc :current-nearest-circle nearest-circle))
                                               (if (< (:distance nearest-circle) (/ diameter 2))
                                                 (om/update-state! this assoc-in [:circles z-index :filled?] true)
                                                 (om/update-state! this assoc-in [:circles z-index :filled?] false))))

                              :onMouseDown (fn [e]
                                             (when (and (= (goog/getValueByKeys e "nativeEvent" "button") 0)
                                                        (not show-size-menu?))
                                               (let [[x y] (get-mouse-location e)]
                                                 (om/update-state! this update :circles conj
                                                                   {:diameter 30 :x x :y y :z-index next-z-index :filled? false})
                                                 (om/update-state! this assoc-in [:circles next-z-index :filled?] true)
                                                 (om/update-state! this update :next-z-index inc))))}
                        (when show-size-menu?
                          (s/menu {:vertical true :compact true
                                   :style {:position "absolute" :top click-y :left click-x
                                           :zIndex 6000000}}
                                  (s/menu-item {:onClick #(do
                                                            (om/update-state! this assoc
                                                                              :show-size-menu? false
                                                                              :resize-circle-modal-open? true))}
                                               "Diameter..")))]
                  (map (partial circle this) circles)))]))
