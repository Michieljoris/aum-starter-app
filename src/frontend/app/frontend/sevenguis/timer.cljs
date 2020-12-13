(ns app.frontend.sevenguis.timer
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [pagora.aum.frontend.util :refer [om-data make-cmp]]
   [goog.object :as goog]
   [pagora.aum.modules.semantic.core :as s]))

(defui ^:once Timer
  Object
  (initLocalState [this]
    {:tick 0 :max-tick 100})
  (render [this]
    (let [{{:keys [tick ticker max-tick]} :state} (om-data this)]
      ;;TODO: turn off ticker in other guis
      (when (not ticker)
        (om/update-state! this assoc :ticker
                          (js/setInterval #(let [{{:keys [tick max-tick]} :state} (om-data this)]
                                             (when (< tick max-tick)
                                               (om/update-state! this update :tick inc))) 100)))

      (html
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
                  "Reset")]))))

(def timer (make-cmp Timer))
