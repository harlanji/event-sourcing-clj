(ns event-sourcing-clj.core
  (:require [event-sourcing-clj.app.todo :as todo-app]
            [event-sourcing-clj.app.daily-log :as dl-app]
            [clojure.core.async :refer [go chan >! <!] :as a]))
; application service accepts command, has reference to repo and aggregate


(def todos (todo-app/todo-service))
(def daily-log (dl/daily-log-service))

(defn load-sample-data []
  (todo-app/create-todo todos "Do a thing")
  (todo-app/create-todo todos "And another one")

  nil)



(defn -main
  [& args]
  (load-sample-data)
  (println todos))