(ns app.frontend.sevenguis.counter
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [pagora.aum.frontend.util :refer [make-cmp]]
   [pagora.aum.modules.semantic.core :as s]))

(defui ^:once Counter
  Object
  (initLocalState [this] {:counter 0})
  (render [this]
    (let [{:keys [counter]} (om/get-state this)]
      (html
       [:div (s/button {:basic true
                        :style {:marginRight 10}
                        :onClick #(om/update-state! this update :counter inc)}
                       "Count")
        counter]))))

(def counter (make-cmp Counter))
