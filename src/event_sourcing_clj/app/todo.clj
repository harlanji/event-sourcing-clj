(ns event-sourcing-clj.app.todo
  (:require [event-sourcing-clj.domain.todo.core :as todo]
            [event-sourcing-clj.domain.todo.model :refer [make-todos]]
            [event-sourcing-clj.infra.aggregate :refer [#_ aggregate!] :as agg]
            [com.stuartsierra.component :as component]
            ))

(defprotocol TodoAppCommands
  (start-app [_])
  (create-todo [_ text])
  (change-text [_ id new-text])
  (mark-done [_ id])
  (delete-todo [_ id])
  ;(clear-done [_])

  (all-todos [_])
  (get-todo [_ id])
  )
(defrecord TodosApp [todo-store
                     reset!
                     swap!]

  component/Lifecycle
  (start [app]
    (start-app app)
    app)
  (stop [app]
    app)

  TodoAppCommands
  (start-app [_]
    (reset! todo-store (make-todos)))
  (create-todo [_ text]
    (let [id (str "todo-" (Math/random))]
      (agg/aggregate! swap! todo-store todo/create-new id text)))
  (change-text [_ id new-text]
    (agg/aggregate! swap! todo-store todo/change-text id new-text))
  (mark-done [_ id]
    (agg/aggregate! swap! todo-store todo/change-completed id true))
  (delete-todo [_ id]
    (agg/aggregate! swap! todo-store todo/delete id))


  (all-todos [_]
    (todo/all-todos @todo-store))
  (get-todo [_ id]
    (todo/get-todo @todo-store id)))