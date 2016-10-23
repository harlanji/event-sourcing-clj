(ns event-sourcing-clj.adapter.web.todos
  (:require [event-sourcing-clj.infra.web :as web-infra]
            [event-sourcing-clj.app.todo :as todos-app]
            [com.stuartsierra.component :as component]
            ))

(defrecord TodoWebAdapter [web-infra
                           todos-app
                           opts]

  web-infra/WebHandler
  (http-request! [_ req]
    (let [todos (todos-app/all-todos todos-app)]
      {:status 200
       :headers {"Content-Type" "text/plain"}
       :body (str "Todos: " todos)}))

  component/Lifecycle
  (start [this]
    (web-infra/start-app web-infra this)
    this)
  (stop [this]
    this))