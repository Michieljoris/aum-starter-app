(ns app.frontend.sevenguis.temperature-converter
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [pagora.aum.frontend.util :refer [make-cmp]]
   [goog.object :as goog]
   [cuerdas.core :as str]))

(defn fahrenheit->celsius [fahrenheit]
  (* (- fahrenheit 32) (/ 5 9)))

(defn celsius->fahrenheit [celsius]
  (+ (* celsius (/ 9 5)) 32))

(defn temperature-input [this type other-type conversion-fn]
  (let [state (om/get-state this)
        label (str/capital (name type))]
    [:div {:class "ui right labeled input"}
     [:input {:type "text"
              :style {:width 60}
              :value (if-let [v (get state type)]
                       (js/Math.round v) "")
              :onChange #(let [value (goog/getValueByKeys % "target" "value")]
                          (om/update-state! this assoc type value)
                          (om/update-state! this assoc other-type (conversion-fn value)))}]
     [:div {:class "ui basic label"} label]]))

(defui ^:once TemperatureConverter
  Object
  (render [this]
    (html
     [:div
      (temperature-input this :celsius :fahrenheit celsius->fahrenheit)
      [:span {:class "pad-lef-5 pad-rig-5"} "="]
      (temperature-input this :fahrenheit :celsius fahrenheit->celsius)])))

(def temperature-converter (make-cmp TemperatureConverter))

