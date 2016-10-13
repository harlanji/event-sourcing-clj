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