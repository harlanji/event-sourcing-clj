(ns event-sourcing-clj.todos.domain.model
  (:require [event-sourcing-clj.todos.domain.core :refer :all]
            [event-sourcing-clj.infra.aggregate :refer [propose accept Aggregate]]))

(defrecord Todos [store]
  Read
  (has-todo? [_ id]
    (contains? store id))
  (all-todos [_]
    (into #{} (vals store)))
  (get-todo [_ id]
    (get store id))

  Write
  (create-todo [model id todo]
    (assoc-in model [:store id] todo))
  (merge-todo [model id attrs]
    (update-in model [:store id] merge attrs))
  (delete-todo [model id]
    (update model :store dissoc id))
  (remove-done [model done-ids]
    (update model :store #(apply dissoc % done-ids)))

  Request
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
    (propose (->ClearDone) model))

  Aggregate
  (aggregate [model event]
    (accept event model)))

(defn make-todos []
  (map->Todos {:store {}}))