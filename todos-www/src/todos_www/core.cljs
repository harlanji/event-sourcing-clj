(ns todos-www.core
  (:require [clojure.pprint :refer [pprint]]))

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

(defn task-ui [task d]
  [:li.task (:name task)])

(defn calendar-day-ui [d]
  [:div.day
   [:p.date (:d d)]
   [:ul.task-list (map task-ui (:tasks d))]])

(defn calendar-grid-ui [date-task-seq]
  (let [calendar-days (map calendar-day-ui date-task-seq)
        week-rows (partition 7 calendar-days)]
    [:div.calendar (map (fn [%] [:div.week %]) week-rows)]))

(defn pprint-html [obj]
  (str "<pre>"
       (with-out-str
         (pprint obj))
       "</pre>"))


(let [body-html (-> (date-seq 1 1 31)
                    with-tasks
                    calendar-grid-ui
                    pprint-html)
      ]
  ; 3 ways to set a JS property
  (set! (-> js/document .-body .-innerHTML) ; Clojure (the thread-first macro)
        ;(.. js/document -body -innerHTML) ; JS Interop (the dot-dot special form)
        ;(.-innerHTML (.-body js/document)) ; LISP (prefix notation)
        body-html)

  )

;; ---

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
)

