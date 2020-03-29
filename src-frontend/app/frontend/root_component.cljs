(ns app.frontend.root-component
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [js.react :as react]
   )
  )


(defui ^:once RootComponent
  static om/IQuery
  (query [this]
    [:client/reload-key])
  Object
  (render [this]
    (html [:div
           "foo5"

           ])
    )
  )
