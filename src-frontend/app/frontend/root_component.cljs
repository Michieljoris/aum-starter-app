(ns app.frontend.root-component
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [js.react :as react]
   [pagora.aum.frontend.util :refer [make-cmp om-data]]
   )
  )

(defui ^:once Foo
  static om/IQuery
  (query [this]
    [:client/foo])
  Object
  (render [this]
    (html [:div "in foo4"])))

(def foo (make-cmp Foo))


(defui ^:once RootComponent
  static om/IQuery
  (query [this]
    [:client/reload-key
     {:user [:id :name]}
     ])
  Object
  (render [this]
    (let [data (om-data this)]
      (timbre/info :#pp {:data data})
      (html [:div
             "Hello"
             ;; (foo this :foo)
             ]))
    )
  )
