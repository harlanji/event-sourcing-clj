(ns todos-www.ui
  (:require [rum.core :refer [defc]]))


(defc task-ui [task]
  [:li.pure-menu-item.no-height [:a.pure-menu-link.task {:href "#"} (:name task)]])


(defc calendar-day-ui [d]
  [:.pure-menu.day
   [:span.pure-menu-heading.date (str "November " (:d d))]
   (into [:ul.pure-menu-list.task-list] (map task-ui (:tasks d)))])

(defc calendar-grid-ui [date-task-seq]
  (let [calendar-days (map calendar-day-ui date-task-seq)
        week-rows (partition 7 calendar-days)]
    (into [:div.calendar] (map (fn [%] (into [:div.week] %)) week-rows))))

(defc layout-ui [content]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1"}]
    [:link {:rel "stylesheet" :href "http://yui.yahooapis.com/pure/0.6.0/pure-min.css"}]
    [:link {:rel "stylesheet" :href "css/style.css"}]]
   [:body
    [:.content
      [:.pure-g
       [:.pure-u-1-6]
       [:.pure-u-2-3
        [:#app content]]
       [:.pure-u-1-6]]]
    [:script {:src "js/compiled/todos_www.js" :type "text/javascript"}]]])

(defc main-ui [model]
      (calendar-grid-ui model))
