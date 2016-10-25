(ns todos-www.core
  (:require [clojure.pprint :refer [pprint]]
            [rum.core :as rum]))


;; ---

(defn date-seq [m from to]
  (map (fn [%] {:m m :d %}) (range from to)))

(defn tasks-for-date [d]
  (if (> 0.25 (rand))
    [{:name (str "Todo " (:d d))}]
    []))

(defn with-tasks [date-seq]
  (map #(assoc % :tasks (tasks-for-date %)) date-seq))

(rum/defc task-ui [task]
  [:li.task (:name task)])

(rum/defc calendar-day-ui [d]
  [:div.day
   [:p.date (:d d)]
   [:ul.task-list {} (map task-ui (:tasks d))]])

(rum/defc calendar-grid-ui [date-task-seq]
  (let [calendar-days (map calendar-day-ui date-task-seq)
        week-rows (partition 7 calendar-days)]
    [:div.calendar (map (fn [%] [:div.week %]) week-rows)]))

(rum/defc main-template [content]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1"}]]
   [:body
    (or content
        [:#app
         [:h2 "Figwheel template"]
         [:p "Check your developer console."]
         [:script {:src "js/compiled/todos_www.js"}]])]])

(rum/defc main-ui []
  (-> (date-seq 1 1 31)
      with-tasks
      calendar-grid-ui))
