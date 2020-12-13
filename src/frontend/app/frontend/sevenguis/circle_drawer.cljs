(ns app.frontend.sevenguis.circle-drawer 
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [pagora.aum.frontend.util :refer [make-cmp]]
   [goog.object :as goog]
   [cuerdas.core :as str]
   [pagora.aum.modules.semantic.core :as s]))

(defn make-circle [filled? {:keys [id radius x y]}]
  (let [diameter (* radius 2)]
    [:div {:id (str "circle-" id)
           :style {:position "absolute"
                   :top (str (- y radius) "px")
                   :left (str (- x radius) "px")
                   :height (str diameter "px")
                   :width (str diameter "px")
                   :background-color  (when (get filled? id) "#bbb")
                   :border "solid black 1px"
                   :border-radius "50%"
                   :display "inline-block"
                   :z-index id}}]))

(defn clip-boxes [box-size max-radius]
  (let [cover-box-size (+ box-size (* 2 max-radius))
        k 1]
    (list [:div {:style {:position "absolute" :width cover-box-size :height max-radius
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
                                      :top (- max-radius) :left (- max-radius)}}])))

(defn get-mouse-location [e]
  (let [x (goog/getValueByKeys e "nativeEvent" "clientX")
        y (goog/getValueByKeys e "nativeEvent" "clientY")
        client-rect (.getBoundingClientRect (goog/getValueByKeys e "currentTarget"))
        x (.round js/Math (- x (goog/getValueByKeys client-rect "left")))
        y (.round js/Math (- y (goog/getValueByKeys client-rect "top")))]
    [x y]))

(defn add-to-history [this circles]
  (let [{:keys [history-index]} (om/get-state this)]
    (om/update-state! this update :history
                      #(conj (subvec % 0 (inc history-index)) circles)))
  (om/update-state! this update :history-index inc))


(defn resize-circle-modal [this {:keys [id x y]}  max-radius]
  (let [close #(do
                 (om/update-state! this assoc :resize-circle-modal-open? false)
                 (om/update-state! this assoc :filled? nil :current-circle nil)
                 (let [{:keys [history history-index]} (om/get-state this)
                       circles (history history-index)
                       circle-el (.getElementById js/document (str "circle-" id))
                       diameter (goog/getValueByKeys circle-el "style" "width") ;;getting back in sync with react
                       diameter (str/slice diameter 0 (- (count diameter) 2))]
                   (add-to-history this (update circles id assoc :radius (/ diameter 2)))))]
    (s/modal {:onClose close
              :open (:resize-circle-modal-open? (om/get-state this))
              :style {:top 410}
              :size "mini"}
             (s/modal-header "Adjust radius of circle")
             (s/modal-content
              (html
               [:div {:class "ui input mar-top-10"
                      :style {:width "100%"}}
                [:input {:type "range"
                         :min 10
                         :max max-radius
                         :value (:radius (om/get-state this))
                         :style {:padding 0}
                         :onChange #(let [value (goog/getValueByKeys % "target" "value")
                                          circle-el (.getElementById js/document (str "circle-" id))
                                          diameter (str (* 2 value) "px")]
                                      ;; Cheating here a bit: no point getting
                                      ;; react involved as long as we stay in sync
                                       (set! (.. circle-el -style -width) diameter)
                                       (set! (.. circle-el -style -height) diameter)
                                       (set! (.. circle-el -style -top) (str (- y value) "px"))
                                       (set! (.. circle-el -style -left) (str (- x value) "px")))} ]]))
             (s/modal-actions
              (s/button {:content "Close" :onClick close})))))

(defn distance [[x1 y1] [x2 y2]]
  (.sqrt js/Math (+ (.pow js/Math (- x1 x2) 2)
                    (.pow js/Math (- y1 y2) 2))))

(defn update-fill [this e current-circle]
  ;; Find nearest circle
  (let [[x y] (get-mouse-location e)
        {:keys [history history-index]} (om/get-state this)
        circle (->> (vals (history history-index))
                    (map #(assoc % :distance
                                 (distance [x y] [(:x %) (:y %)])))
                    (filter #(<= (:distance %) (:radius %)))
                    (apply min-key :distance))]

    ;; Color circle grey if mouse is in it
    (when circle
      (om/update-state! this assoc-in [:filled? (:id circle)] true))

    ;; Unfill any previous filled circles
    (when (not= (:id circle) (:id current-circle))
      (om/update-state! this assoc-in [:filled? (:id current-circle)] false)
      (om/update-state! this assoc :current-circle circle))))

(defn canvas [this box-size circles]
  (let [{:keys [next-id show-size-menu? current-circle filled?]} (om/get-state this)]
    [:div {:style {:width box-size :height box-size :border "1px solid lightgrey"}
           :onContextMenu (fn [e]
                            (if show-size-menu? ;;turn off and update filling
                              (do (om/update-state! this assoc :show-size-menu? false)
                                  (update-fill this e current-circle))
                              ;;else show size menu if mouse in circle:
                              (when current-circle
                                (let [[x y] (get-mouse-location e)]
                                  ;; Remember location of click for positioning of popup menu
                                  (om/update-state! this assoc :click-x x :click-y y :show-size-menu? true))))
                            (.preventDefault e))

           ;; Track mouse for filling of circles, but suspend when popup menu is
           ;; showing
           :onMouseMove #(when (not show-size-menu?) (update-fill this % current-circle))

           ;; New circle
           :onMouseDown (fn [e]
                          (when show-size-menu? ;;mouse could be anywhere, tracking was suspended
                            (om/update-state! this assoc :show-size-menu? false)
                            (update-fill this e current-circle)) ;; so update filling current-circle

                          ;;Create a new circle with left mouse button if not in circle
                          (when (and (= (goog/getValueByKeys e "nativeEvent" "button") 0)
                                     (not (:current-circle (om/get-state this))))
                            (let [[x y] (get-mouse-location e)
                                  new-circle {:id next-id :x x :y y :radius 15}]
                              (add-to-history this (assoc circles next-id new-circle))
                              (om/update-state! this assoc-in [:filled? next-id] true)
                              (om/update-state! this assoc :current-circle new-circle)
                              (om/update-state! this update :next-id inc))))}
     (map (partial make-circle filled?) (vals circles))]))

(defn size-popup-menu [this]
  (let [{:keys [click-x click-y current-circle]} (om/get-state this)]
   (s/menu {:vertical true :compact true
            :style {:position "absolute" :top click-y :left click-x
                    :zIndex 6000000}}
           (s/menu-item {:onClick #(do (om/update-state! this assoc :radius (:radius current-circle))
                                       (om/update-state! this assoc
                                                         :show-size-menu? false
                                                         :resize-circle-modal-open? true))}
                        "Diameter.."))))

(defn div
  "For easy building of an div: unpack lists, remove any nil"
  [& xs]
    (reduce
     #(letfn [(aconj [x y] (apply conj x y))]
        (if (seq? %2)
          (aconj %1 %2)
          (if (some? %2)
            (conj %1 %2)
            %1)))
     [:div] xs))

(defui ^:once CircleDrawer
  static om/IQuery
  (query [this] [])
  Object
  (initLocalState [this]
    {:history [{}]
     :history-index 0
     :filled? {}
     :next-id 1})
  (render [this]
    (let [{:keys [show-size-menu? history history-index current-circle]} (om/get-state this)
          box-size 300
          max-radius 30
          circles (history history-index)]
      (html
       [:div {:style { :paddingLeft max-radius}}

        ;; Undo/redo buttons
        [:div {:class "pad-bot-30"
               :style {:display "flex"
                       :justify-content "center"
                       :width box-size}}
         (s/button {:basic true :content "Undo"
                    :disabled (= history-index 0)
                    :onClick #(do (om/update-state! this assoc :filled? nil :current-circle nil :show-size-menu? nil)
                                  (om/update-state! this assoc :history-index (dec history-index)))})
         (s/button {:basic true :content "Redo"
                    :disabled (= history-index (dec (count history)))
                    :onClick #(do (om/update-state! this assoc :filled? nil :current-circle nil)
                                  (om/update-state! this assoc :history-index (inc history-index)))})]

        ;; Resize modal
        (resize-circle-modal this current-circle max-radius)

        ;; Circles in a box
        (div {:style {:position "relative"}}
          ;; Hide parts of circle outsize box, positioned absolutely around canvas
          (clip-boxes box-size max-radius)
          ;; Canvas containing absolutely positioned circles
          (canvas this box-size circles)
          (when show-size-menu? (size-popup-menu this)))]))))


(def circle-drawer (make-cmp CircleDrawer))
