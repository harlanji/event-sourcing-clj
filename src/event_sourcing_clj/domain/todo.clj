(ns event-sourcing-clj.domain.todo
  (:require [event-sourcing-clj.infra.aggregate :refer [Proposer propose Acceptor accept] :as agg]))

; -- value objects
(deftype TodoId [id])

; -- entities
(defrecord Todo [id text completed?]
  agg/Entity
  (id [_] (->TodoId id)))

; -- events
(declare ->Created
         ->TextChanged
         ->CompletedChanged
         ->Deleted
         ->DoneCleared)

; -- commands (requests)
(declare ->CreateNew
         ->CreateCompleted ; second create signature
         ->ChangeText
         ->ChangeCompleted
         ->Delete
         ->ClearDone)

(defprotocol TodoQueries
  (has-todo? [_ id])
  (all-todos [_])
  (get-todo [_ id]))

(defprotocol TodoCommands
  (create-new [_ id text])
  (change-text [_ id new-text])
  (change-completed [_ id completed?])
  (delete [_ id])
  (clear-done [_]))


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
  (create-completed [model id text]
    (propose (->CreateCompleted id text) model))
  (change-text [model id new-text]
    (propose (->ChangeText id new-text) model))
  (change-completed [model id completed?]
    (propose (->ChangeCompleted id completed?) model))
  (delete [model id]
    (propose (->Delete id) model))
  (clear-done [model]
    (propose (->ClearDone) model)))


; -- create a new todo

(defrecord Created [id text completed?]
  Acceptor
  (accept [_ model]
          (let [todo (->Todo id text completed?)]
            (assoc-in model [:store id] todo))))

(defrecord CreateNew [id text]
  Proposer
  (propose [_ model]
    (when-not (has-todo? model id)
      (->Created id text false))))

; example use case... multiple proposers for one event
(defrecord CreateComplete [id text]
  Proposer
  (propose [_ model]
    (when-not (has-todo? model id)
      (->Created id text true))))


; -- change text of a todo

(defrecord TextChanged [id new-text]
  Acceptor
  (accept [_ model]
    (assoc-in model [:store id :text] new-text)))


(defrecord ChangeText [id new-text]
  Proposer
  (propose [_ model]
    (when (has-todo? model id)
      (->TextChanged id new-text))))

; -- change completed status of a todo

(defrecord CompletedChanged [id completed?]
  Acceptor
  (accept [_ model]
    (assoc-in model [:store id :completed?] completed?)))


(defrecord ChangeCompleted [id completed?]
  Proposer
  (propose [_ model]
    (when (has-todo? model id)
      (->CompletedChanged id completed?))))



; -- delete a todo

(defrecord Deleted [id]
  Acceptor
  (accept [_ model]
    (update model :store dissoc id)))


(defrecord Delete [id]
  Proposer
  (propose [_ model]
    (when (has-todo? model id)
      (->Deleted id))))



; -- clear done todos

(defrecord DoneCleared [ids]
  Acceptor
  (accept [_ model]
    (update model :store #(apply dissoc % ids))))


(defrecord ClearDone []
  Proposer
  (propose [_ model]
    (let [done-ids (->> (all-todos model)
                        (filter :completed?)
                        (map :id))
          done-ids (into #{} done-ids)]
      (->DoneCleared done-ids))))


; -- make a service

(defn make-todos []
  (map->Todos {:store {}}))