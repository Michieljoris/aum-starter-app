(ns app.frontend.sevenguis.counter
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [pagora.aum.frontend.util :refer [make-cmp om-data]]
   [pagora.aum.modules.semantic.core :as s]))

(defui ^:once Counter
  static om/Ident
  (ident [this props]
    [:cmp :counter])
  Object
  (initLocalState [this] {:counter 0})
  (render [this]
    (let [{{:keys [counter]} :state} (om-data this)]
     
      (html
       [:div#counter (s/button {:basic true
                        :style {:marginRight 10}
                        :onClick #(om/update-state! this update :counter inc)}
                       "Count")
        counter]))))

(def counter (make-cmp Counter))
