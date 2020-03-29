(ns app.frontend.root-component
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [js.react :as react]
   )
  )


(defui ^:once RootComponent
  Object
  (render [this]
    (html [:div
           "2 Hello!!!!!"
           ])
    )
  )
