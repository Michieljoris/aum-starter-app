(ns app.frontend.cells
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [pagora.aum.frontend.util :refer [make-cmp om-data]]
   [goog.object :as goog]
   [cuerdas.core :as str]
   [app.frontend.semantic :as s]))


(defui ^:once Cells
  static om/IQuery
  (query [this] [])
  Object
  (initLocalState [this]
   {})
  (render [this]
    (let [{{:keys []} :state} (om-data this)]
      (html
       [:div {:style {}}
        "Cells"



        ]))))


(def cells (make-cmp Cells))
