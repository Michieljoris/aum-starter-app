(ns app.frontend.core
  (:require
   [app.frontend.config]
   [pagora.aum.frontend.core :as aum]
   [sablono.core :as html :refer-macros [html]]
   [taoensso.timbre :as timbre]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [js.react :as react]
   ))

(defui ^:once RootComponent

  Object
  (render [this]
    (html [:div
           "Hello!!!!!"
           ])
    )
  )

(let [_ :foo]
  (let [aum-config (aum/init {:RootComponent RootComponent
                              })]
    (aum/go aum-config))
  )

(js/console.log react)
