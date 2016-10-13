(ns event-sourcing-clj.domain.todo.model
  (:require [event-sourcing-clj.domain.todo.core :refer :all]
            [event-sourcing-clj.infra.aggregate :refer [propose]]))

; read model / projection
(defrecord Todos [store]
  TodoQueries
  (has-todo? [model id]
    (contains? store id))
  (all-todos [model]
    (into #{} (vals store)))
  (get-todo [model id]
    (get store id))

  TodoChangers
  (create-todo [model id todo]
    (assoc-in model [:store id] todo))
  (merge-todo [model id attrs]
    (update-in model [:store id] merge attrs))
  (delete-todo [model id]
    (update model :store dissoc id))
  (clear-done [model done-ids]
    (update model :store #(apply dissoc % done-ids)))

  ; interface for editor completion... usefulness can be debated
  ; could be useful for MAPPING VIEW PROJECTION
  TodoCommands
  (create-new [model id text]
    (propose (->CreateNew id text) model))
  (create-complete [model id text]
    (propose (->CreateComplete id text) model))
  (change-text [model id new-text]
    (propose (->ChangeText id new-text) model))
  (change-completed [model id completed?]
    (propose (->ChangeCompleted id completed?) model))
  (delete [model id]
    (propose (->Delete id) model))
  (clear-done [model]
    (propose (->ClearDone) model)))


(defn make-todos []
  (map->Todos {:store {}}))