(ns event-sourcing-clj.core
  (:require [event-sourcing-clj.app.todo :as todo-app]
            [avout.core :as avout]
            [com.stuartsierra.component :as component]))

(def avout-client (avout/connect "127.0.0.1"))

(defn make-todo-system []
  (-> (component/system-map :todo-store (avout/zk-atom avout-client "/todos/dev")
                            :todos-app  (todo-app/map->TodosApp {:reset! avout/reset!!
                                                                 :swap! avout/swap!!}))
      (component/system-using {:todos-app {:todo-store :todo-store}})))

(defn load-sample-data [system]
  (let [todos-app (:todos-app system)]
    (todo-app/create-todo todos-app "Do a thing")
    (todo-app/create-todo todos-app "And another one")

    (println (todo-app/all-todos todos-app))))



(defn -main
  [& args]
  (let [system (component/start (make-todo-system))]
    (load-sample-data system)))