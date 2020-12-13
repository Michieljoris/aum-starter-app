(ns app.frontend.cells-grammar
  (:require
   [taoensso.timbre :as timbre]
   [cuerdas.core :as str]
   [cljs.reader :refer [read-string]]
   [instaparse.core :as insta]))

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

(defn cell-str [{value :value formula :formula}]
  (if (= Textual (type formula))
    (to-str formula)
    (str value)))
