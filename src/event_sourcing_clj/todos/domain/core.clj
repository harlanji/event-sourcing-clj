(ns event-sourcing-clj.todos.domain.core
  (:require [event-sourcing-clj.infra.aggregate
             :refer [Proposer propose Acceptor accept]
             :as agg]))


; -- value objects
(deftype TodoId [id])

; -- entities
(defrecord Todo [id text completed?]
  agg/Entity
  (id [_] (->TodoId id)))

; -- model / projection interfaces
(defprotocol Read
  (has-todo? [_ id])
  (all-todos [_])
  (get-todo [_ id]))

(defprotocol Write
  (create-todo [_ id todo])
  (merge-todo [_ id attrs])
  (delete-todo [_ id])
  (remove-done [_ done-ids]))

(defprotocol Propose
  (create-new [_ id text])
  (create-complete   [_ id text])
  (change-text [_ id new-text])
  (change-completed [_ id completed?])
  (delete [_ id])
  (clear-done [_]))

; -----------------------------------------------------------------------------

; -- create a new todo

(defrecord Created [id text completed?]
  Acceptor
  (accept [_ model]
    (let [todo (->Todo id text completed?)]
      (create-todo model id todo))))

(defrecord CreateNew [id text]
  Proposer
  (propose [_ model]
    (when-not (has-todo? model id)
      (->Created id text false))))

; example use case... multiple proposers for one event
; this is why events are before proposers (state transitions + validator)
; event binding stuff my be like events... text changed/text complete (built in event stuff)
(defrecord CreateComplete [id text]
  Proposer
  (propose [_ model]
    (when-not (has-todo? model id)
      (->Created id text true))))


; -- change text of a todo
; state transitions
; idea: opennlp to autogen names by transform
; idea: Rails - one powerful idea is pluralizing... since it's a collection oriented system
(defrecord TextChanged [id new-text]
  Acceptor
  (accept [_ model]
    (merge-todo model id (map->Todo {:text new-text}))))


(defrecord ChangeText [id new-text]
  Proposer
  (propose [_ model]
    (when (has-todo? model id)
      (->TextChanged id new-text))))

; -- change completed status of a todo

(defrecord CompletedChanged [id completed?]
  Acceptor
  (accept [_ model]
    (merge-todo model id (map->Todo {:completed? completed?}))))


(defrecord ChangeCompleted [id completed?]
  Proposer
  (propose [_ model]
    (when (has-todo? model id)
      (->CompletedChanged id completed?))))



; -- delete a todo

(defrecord Deleted [id]
  Acceptor
  (accept [_ model]
    (delete-todo model id)))


(defrecord Delete [id]
  Proposer
  (propose [_ model]
    (when (has-todo? model id)
      (->Deleted id))))



; -- clear done todos

(defrecord DoneCleared [done-ids]
  Acceptor
  (accept [_ model]
    (remove-done model done-ids)))


; many values, one transaction
(defrecord ClearDone []
  Proposer
  (propose [_ model]
    (let [done-ids (->> (all-todos model)
                        (filter :completed?)
                        (map :id))
          done-ids (into #{} done-ids)]
      (->DoneCleared done-ids))))

