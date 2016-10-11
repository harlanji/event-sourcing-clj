(ns event-sourcing-clj.app.todo
  (:require [event-sourcing-clj.domain.todo :as todo]
            [event-sourcing-clj.infra.aggregate :as agg]
            ))

(defprotocol TodoAppCommands
  (create-todo [_ text])
  (change-text [_ id new-text])
  (mark-done [_ id])
  (delete [_ id])
  ;(clear-done [_])

  (all-todos [_])
  (get-todo [_ id])
  )

; responsible for accepting commands and transactions
; and emiting events. eg cross-aggregate transactions can use refs.
;
; note some domain logic has leaked into here... change-text + mark-down
; might be another semantic smell.. several things using the same model method signature
(defrecord TodosApp
  [todos]

  TodoAppCommands
  (create-todo [_ text]
    (let [id (str "todo-" (Math/random))]
      (agg/app-atom-command todos todo/create-new id text)))
  (change-text [_ id new-text]
    (agg/app-atom-command todos todo/change-text id new-text))
  (mark-done [_ id]
    (agg/app-atom-command todos todo/set-completed id true))
  (delete [_ id]
    (agg/app-atom-command todos todo/delete id))

  (all-todos [_]
    (todo/all-todos @todos))
  (get-todo [_ id]
    (todo/get-todo @todos id)))



; idea: macro for generic single domain service with atom repo, as well as core.async + kv store


(defn todo-service []
  (let [todos (atom (todo/map->TodosAggregate {}))]
    (map->TodosApp {:todos todos})))