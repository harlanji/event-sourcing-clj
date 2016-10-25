(ns todos-www.core
  (:require [clojure.pprint :refer [pprint]]
            [rum.core :as rum]))

(enable-console-print!)


;; ---

(defn date-seq [m from to]
  (map (fn [%] {:m m :d %}) (range from to)))

(defn tasks-for-date [d]
  (if (> 0.25 (rand))
    [{:name (str "Thing on " (:d d))}]
    []))

(defn with-tasks [date-seq]
  (map #(assoc % :tasks (tasks-for-date %)) date-seq))

(rum/defc task-ui [task d]
  [:li.task (:name task)])

(rum/defc calendar-day-ui [d]
  [:div.day
   [:p.date (:d d)]
   [:ul.task-list (map task-ui (:tasks d))]])

(rum/defc calendar-grid-ui < rum/static [date-task-seq]
  (let [calendar-days (map calendar-day-ui date-task-seq)
        week-rows (partition 7 calendar-days)]
    [:div.calendar (map (fn [%] [:div.week %]) week-rows)]))

(defn main []
  (let [app-dom (.getElementById js/document "app")
        app-ui (-> (date-seq 1 1 31)
                   with-tasks
                   calendar-grid-ui)
        ]
    (rum/mount app-ui app-dom)))

;; ---

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
)

(main)