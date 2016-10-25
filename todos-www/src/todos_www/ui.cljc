(ns todos-www.ui
  (:require [rum.core :refer [defc]]))


(defc task-ui [task]
  [:li.task (:name task)])

(defc calendar-day-ui [d]
  [:div.day
   [:p.date (:d d)]
   [:ul.task-list {} (map task-ui (:tasks d))]])

(defc calendar-grid-ui [date-task-seq]
  (let [calendar-days (map calendar-day-ui date-task-seq)
        week-rows (partition 7 calendar-days)]
    [:div.calendar (map (fn [%] [:div.week %]) week-rows)]))

(defc layout-ui [content]
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
         ])
    [:script {:src "js/compiled/todos_www.js" :type "text/javascript"}]
    ]])

(defc main-ui [model]
      (calendar-grid-ui model))
