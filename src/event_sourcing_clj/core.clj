(ns event-sourcing-clj.core
  (:require [event-sourcing-clj.app.todo :as todo-app]
            ;[event-sourcing-clj.app.daily-log :as dl-app]
            [avout.core :as avout]
            [clojure.core.async :refer [go chan >! <!] :as a]))
; application service accepts command, has reference to repo and aggregate

(def avout-client (avout/connect "127.0.0.1"))

; too much for high throughput systems, but it's rally a log
(def zk-todos (avout/zk-atom avout-client "/todos/dev"))

(def todos (todo-app/make-todo! zk-todos avout/reset!! avout/swap!!))
;(def daily-log (dl/daily-log-service))

(defn load-sample-data []
  (todo-app/create-todo todos "Do a thing")
  (todo-app/create-todo todos "And another one")

  (println (todo-app/all-todos todos))

  nil)



(defn -main
  [& args]
  (load-sample-data)
  (println todos))