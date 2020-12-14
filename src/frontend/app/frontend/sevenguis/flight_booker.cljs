(ns app.frontend.sevenguis.flight-booker
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [goog.object :as goog]
   [pagora.aum.frontend.util :refer [make-cmp om-data]]
   [pagora.aum.modules.semantic.core :as s]))

(defn date? [m]
  (.isValid m) )

(def flight-options
  [{:text "One way flight"
     :value :one-way-flight}
   {:text "Return flight"
    :value :return-flight}])

(defn enter-date [this date-type {:keys [error? disabled?]}]
  (let [state (om/get-state this)
        date (state date-type)]
    [:div {:class (cond-> "ui left labeled input mar-top-10"
                    error? (str " error"))}
     [:input {:type "text" :value (or date "")
              :style {:width 110}
              :disabled disabled?
              :onChange #(let [value (goog/getValueByKeys % "target" "value")]
                           (om/update-state! this assoc date-type value))} ]]))

(defui ^:once FlightBooker
  Object
  (initLocalState [this]
    {:flight-type :one-way-flight
     :leave-date "24.12.2020"
     :return-date "24.12.2020"
     })
  (render [this]
    (let [{:keys [flight-type leave-date return-date]} (om/get-state this)
          leave-moment (js/moment leave-date "DD.MM.YYYY" true)
          return-moment (js/moment return-date "DD.MM.YYYY" true)
          valid-leave-date? (date? leave-moment)
          valid-return-date? (date? return-moment)]

      (html
       [:div#flight-booker
        ;; Select flight type
        (s/dropdown {:inline true
                     :options flight-options
                     :onChange  #(om/update-state! this assoc
                                                   :flight-type (keyword (goog/getValueByKeys %2 "value")))
                     :value flight-type}) [:br]

        ;; Enter leave date
        (enter-date this  :leave-date {:error? (not valid-leave-date?)
                                       :disabled? false}) [:br]

        ;; Enter return date
        (enter-date this  :return-date {:error? (not valid-return-date?)
                                        :disabled? (= flight-type :one-way-flight)}) [:br]

        ;; Book flight
        (s/button {:basic true
                   :style {:marginTop 10}
                   :disabled (case flight-type
                               :one-way-flight (not valid-leave-date?)
                               :return-flight (or (not valid-leave-date?) (not valid-return-date?)
                                                  (.isSameOrAfter leave-moment return-moment)))
                   :onClick #(om/update-state! this assoc :flight-booker-modal-open? true)}
                  "Book")

        ;; Dialog
        (s/modal
         {:onClose #(om/update-state! this assoc :flight-booker-modal-open? false)
          :size "tiny"
          :open (:flight-booker-modal-open? (om/get-state this))
          :content (case flight-type
                     :one-way-flight (str "You booked a one-way flight on " leave-date)
                     :return-flight (str "You booked a return flight on " leave-date " and " return-date))
          :actions [{:key "ok", :content "OK", :positive true }]})]))))

(def flight-booker (make-cmp FlightBooker))
