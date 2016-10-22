(ns event-sourcing-clj.app.todo
  (:require [event-sourcing-clj.domain.todo.core :as todo]
            [event-sourcing-clj.domain.todo.model :refer [make-todos]]
            [event-sourcing-clj.infra.aggregate :refer [aggregate!] :as agg]

            ))

(defprotocol TodoAppCommands
  (create-todo [_ text])
  (change-text [_ id new-text])
  (mark-done [_ id])
  (delete-todo [_ id])
  ;(clear-done [_])

  (all-todos [_])
  (get-todo [_ id])
  )

(defrecord TodosApp [todos
                     reset!
                     swap!]

  TodoAppCommands
  (create-todo [_ text]
    (let [id (str "todo-" (Math/random))]
      (aggregate! swap! todos todo/create-new id text)))
  (change-text [_ id new-text]
    (aggregate! swap! todos todo/change-text id new-text))
  (mark-done [_ id]
    (aggregate! swap! todos todo/change-completed id true))
  (delete-todo [_ id]
    (aggregate! swap! todos todo/delete id))

  (all-todos [_]
    (todo/all-todos @todos))
  (get-todo [_ id]
    (todo/get-todo @todos id)))

(defn make-todo! [todos-atom reset! swap!]
  (map->TodosApp {:todos todos-atom :reset! reset! :swap! swap!}))