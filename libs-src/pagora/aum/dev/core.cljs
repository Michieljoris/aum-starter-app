(ns ^:figweel-load ^:figwheel-hooks pagora.aum.dev.core
  (:require
   [pagora.aum.om.next :as om]
   [taoensso.timbre :as timbre]))


(def reconciler)
(def RootComponent)
(def app-state)




(defn ^:after-load my-after-reload-callback []
  "Gets called on source changes"
  (timbre/info :#g "=============== RELOAD ===============")
  (do
    (when (om/mounted? (om/class->any reconciler RootComponent))
      (do
        ;;Because currently we add a callback to computed in root component, if we
        ;;force the root component to update all the child components that pass on
        ;;computed with that fn will also update
        ;; (.forceUpdate (om/class->any reconciler RootComponent))

        ;; Alternatively we can query for client/on-reload-key in root cmp and
        ;; pass that along in computed, or add it to queries for the components we
        ;; want to rerender on reload. Again, only cmps that pass along computed with
        ;; this reload key will update.
        (let [uuid (random-uuid)]
          ;; (timbre/info :#g "uuid:" uuid)
          (swap! app-state assoc :client/reload-key uuid))

        ;;  Doesn't work with react 16+
        ;; (rerender-react-tree reconciler)
        )
      ;; (put! (get-or-make-channel) {:event :on-jsload :data {}}
      ;;       #(timbre/info "Sent msg :on-jsload"))
      )))
