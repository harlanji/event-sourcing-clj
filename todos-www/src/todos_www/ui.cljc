(ns todos-www.ui
  (:require [rum.core :refer [defc]]))


(defc task-ui [task]
  [:li.task (:name task)])

(defc calendar-day-ui [d]
  [:div.day
   [:p.date (:d d)]
   (into [:ul.task-list] (map task-ui (:tasks d)))])

(defc calendar-grid-ui [date-task-seq]
  (let [calendar-days (map calendar-day-ui date-task-seq)
        week-rows (partition 7 calendar-days)]
    (into [:div.calendar] (map (fn [%] (into [:div.week] %)) week-rows))))

(defc layout-ui [content]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1"}]]
   [:body
    [:#app content]
    [:script {:src "js/compiled/todos_www.js" :type "text/javascript"}]]])

(defc main-ui [model]
      (calendar-grid-ui model))
