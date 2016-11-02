(ns todos-www.core
  (:require [clojure.pprint :refer [pprint]]))

(defn date-seq [m from to]
  (map (fn [%] {:m m :d %}) (range from to)))

(defn tasks-for-date [d]
  (if (even? (:d d))
    [{:name (str "Todo " (:d d))}]
    []))

(defn with-tasks [date-seq]
  (map #(assoc % :tasks (tasks-for-date %)) date-seq))

(defn make-model []
  (-> (date-seq 1 1 31)
      with-tasks))