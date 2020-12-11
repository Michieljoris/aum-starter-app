(ns app.frontend.cells
  (:require
   [sablono.core :as html :refer-macros [html]]
   [pagora.aum.om.next :as om :refer-macros [defui]]
   [taoensso.timbre :as timbre]
   [pagora.aum.frontend.util :refer [make-cmp om-data]]
   [goog.object :as goog]
   [cuerdas.core :as str]
   [cljs.reader :refer [read-string]]
   [instaparse.core :as insta]
   [app.frontend.semantic :as s]))

(def alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ")

(defprotocol Formula
  (evaluate   [this data])
  (refs   [this data])
  (to-str [this]))

(defrecord Textual [value]
  Formula
  (evaluate   [this data] 0.0)
  (refs   [this data] [])
  (to-str [this] value))

(defrecord Decimal [value]
  Formula
  (evaluate   [this data] value)
  (refs   [this data] [])
  (to-str [this] (str value)))

(defrecord Coord [row column]
  Formula
  (evaluate   [this data] (:value (get data [row column])))
  (refs   [this data] [(get data [row column])])
  (to-str [this] (str (get alphabet column) row)))

(defrecord Reange [coord1 coord2]
  Formula
  (evaluate   [this data] js/NaN)
  (refs   [this data] (for [row (range (:row coord1) (inc (:row coord2)))
                            col (range (:column coord1) (inc (:column coord2)))]
                        (get data [row col])))
  (to-str [this] (str (to-str coord1) ":" (to-str coord2))))

(defn eval-list [formula data]
  (if (= Reange (type formula))
    (map #(:value %) (refs formula data))
    [(evaluate formula data)]))

(def op-table
  {"add" #(+ %1 %2)
   "sub" #(- %1 %2)
   "div" #(/ %1 %2)
   "mul" #(* %1 %2)
   "mod" #(mod %1 %2)
   "sum" +
   "prod" *})

(defrecord Application [function arguments]
  Formula
  (evaluate   [this data]
    (let [argvals (mapcat #(eval-list % data) arguments)]
      (try
        (apply (get op-table function) argvals)
        (catch :default e js/NaN))))
  (refs   [this data] (mapcat #(refs % data) arguments))
  (to-str [this] (str function "(" (str/join ", " (map to-str arguments)) ")")))

(def Emptie (Textual. ""))

(defn parse-formula [formula-str]
  (let [result
         ((insta/parser "
          formula = decimal / textual / (<'='> expr)
          expr    = range / cell / decimal / app
          app     = ident <'('> (expr <','>)* expr <')'>
          range   = cell <':'> cell
          cell    = #'[A-Za-z]\\d+'
          textual = #'[^=].*'
          ident   = #'[a-zA-Z_]\\w*'
          decimal = #'-?\\d+(\\.\\d*)?'
          ") formula-str)]
    (if (insta/failure? result)
      (Textual. "Error" ;; (str (insta/get-failure result))
                )
      (insta/transform
        {:decimal #(Decimal. (js/parseFloat %))
         :ident   str
         :textual #(Textual. %)
         :cell    #(Coord. (read-string (subs % 1))
                           (.indexOf alphabet (first %)))
         :range   #(Reange. %1 %2)
         :app     (fn [f & as]
                    (Application. f (vec as)))
         :expr    identity
         :formula identity
         } result))))

(defn cell-at [this x y]
  (get-in (om/get-state this) [:cells [x y]]))

(defn cell-str [{value :value formula :formula}]
  (if (= Textual (type formula))
    (to-str formula)
    (str value)))

;; ==================================================

(defn change-prop [this {:keys [r c value formula observers]}]
  (let [cells (:cells (om/get-state this))
        new-value  (evaluate formula cells)]
    (when-not (or (= value new-value) (and (js/isNaN value) (js/isNaN new-value)))
      (om/update-state! this assoc-in [:cells [r c] :value] new-value)
      (doseq [[r c] observers] (change-prop this (cell-at this r c))))))

(defn update-cell [this event r c cells]
  (let [content (or (goog/getValueByKeys event "target" "value") "")
        formula (if (empty? content)
                  Emptie
                  (parse-formula content))
        oldform (:formula (cells [r c]))]
    (doseq [cell (refs oldform cells)]
      (om/update-state! this update-in [:cells [(:r cell) (:c cell)] :observers]
                        (fn [obs] (remove #(= % [r c]) obs))))
    (doseq [cell (refs formula cells)]
      (om/update-state! this update-in [:cells [(:r cell) (:c cell)] :observers]
             #(conj % [r c])))

    (om/update-state! this update-in [:cells [r c]] assoc :formula formula :content content)
    (change-prop this (cell-at this r c))))

(defn table [this cells size]
  (let [{:keys [active-cell]} (om/get-state this)]
    [:div {:style {:width size :overflow "auto"
                   :height size }}
     [:div {:style {:width 2500}}
      (s/table
       {:definition true :compact "very" :celled true :size "small"}
       (s/table-header {:fullWidth true }
                       (apply s/table-row
                              (s/table-header-cell)
                              (map
                               #(s/table-header-cell {:textAlign "center"} %)
                               (seq alphabet))))
       (apply s/table-body
              (map
               (fn [r]
                 (apply s/table-row
                        (s/table-cell {:collapsing true} r)
                        (map (fn [c]
                               (s/table-cell
                                {:id (+ r c)
                                 :style {:maxWidth (/ size 26)}
                                 :onClick (fn []
                                            (when (not= [r c] active-cell)
                                              (om/update-state! this assoc :active-cell nil)))
                                 :onKeyPress #(let [char-code (goog/get % "which")]
                                                (when (= (char char-code) \return)
                                                  (update-cell this % r c cells)
                                                  (om/update-state! this assoc :active-cell nil)))
                                 :onDoubleClick #(om/update-state! this assoc :active-cell [r c])}
                               
                                (html
                                 (if (= active-cell [r c])
                                   (s/input {:fluid true  :transparent true :autoFocus true
                                             :defaultValue (get-in cells [[r c] :content])
                                             :onBlur #(update-cell this % r c cells)})
                                   [:div {:style {:overflow "hidden"}}
                                    [:span (cell-str (cells [r c]))]]))))
                             (range 26))))
               (range 100))))]]))

(defn make-data [height width]
  (into {} (for [r (range height) c (range width)]
             [[r c] {:r r :c c :value "" :formula Emptie :observers []}])))

(defui ^:once Cells
  static om/IQuery
  (query [this] [])
  Object
  (initLocalState [this]
   {:cells (make-data 100 26)})
  (render [this ]
    (let [{{:keys [cells]} :state} (om-data this)]
      (html
       (table this cells 600)))))

(def cells (make-cmp Cells))

;;============================================
;; Scratchpad

(defui ^:once Input
  static om/IQuery
  (query [this] [:value])
  Object
  (initLocalState [this]
    (:props (om-data this)))
  (render [this]
    (let [{:keys [state props]} (om-data this)]
      (timbre/info :#pp props)

      (s/input {:fluid true  :transparent true
                ;; :value (or (get-in (om/get-state this) [:cells [r c] :value]) "")
                :defaultValue (or (:value (om/get-state this)) "")
                :onBlur #(do

                           (let [value (goog/getValueByKeys % "target" "value")
                                 formula (parse-formula value)]
                             (timbre/info value)
                             (timbre/info formula)
                             ;; (om/update-state! this update-in [:cells [r c]]
                             ;;                   (fn [c]
                             ;;                     (timbre/info :#pp c)

                             ;;                     (assoc c formula :formula
                             ;;                            :value value)))
                             )
                           (timbre/info "blur"))
                ;; :onFocus #(update-state! assoc )
                :onChange #(let [value (goog/getValueByKeys %2 "value")]
                             (om/update-state! this assoc :value value)
                             ;; (om/update-state! this update-in [:cells [r c]]
                             ;;                   assoc :value value
                             ;;                   )
                             )
                }))
    )
  )
(def input (make-cmp Input))

      ;; ;; (js/console.log "formula:" (to-str (get-in (om/get-state this) [:cells [0 0] :formula])))
      ;; (let [formula (parse-formula "123")
      ;;       ;; formula (parse-formula "=A1")
      ;;       ]
      ;;   ;; (js/console.log formula)
      ;;   ;; (js/console.log (cell-str {:formula formula :value (evaluate formula, cells) ;; :type  (type formula)
      ;;   ;;                            }))
      ;; ;;   ;; (if (= Textual (type formula))
      ;; ;;   ;;   (to-str formula)
      ;; ;;   ;;   (str value))
      ;; ;;   ;; (js/console.log "????" (evaluate formula cells))
      ;; ;;   ;; (js/console.log (parse-formula "=A1:A2"))
      ;; ;;   ;; (js/console.log "eval:" (evaluate formula cells))
      ;; ;;   ;; (js/console.log "refs:" (refs formula cells))
      ;; ;;   ;; (js/console.log "to-str:" (to-str formula))
      ;; ;;   ;; (js/console.log (parse-formula "123.1"))
      ;;   )
