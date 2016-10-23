(ns event-sourcing-clj.core
  (:require [event-sourcing-clj.todos.app :as todo-app]
            [event-sourcing-clj.todos.web :as todos-web]
            [event-sourcing-clj.infra.web :as web-infra]
            [com.stuartsierra.component :as component]))


(defn make-todo-system []
  (-> (component/system-map :todo-store (atom nil)
                            :todos-app  (todo-app/map->TodosApp {:reset! reset!
                                                                 :swap! swap!})

                            :web-config {:port 8080}
                            :web-apps-store (atom [])
                            :web-infra (web-infra/map->WebApp {})

                            :todo-web (todos-web/map->TodoWebAdapter {})
                            )
      (component/system-using {:todos-app {:todo-store :todo-store}
                               :web-infra {:opts :web-config
                                           :apps :web-apps-store}

                               :todo-web [:web-infra :todos-app]
                               })))

(defn load-sample-data [system]
  (let [todos-app (:todos-app system)]
    (todo-app/create-todo todos-app "Do a thing")
    (todo-app/create-todo todos-app "And another one")

    (println (todo-app/all-todos todos-app))))



(defn -main
  [& args]
  (let [system (component/start (make-todo-system))]
    (load-sample-data system)))