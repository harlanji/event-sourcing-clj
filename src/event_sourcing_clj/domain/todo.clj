(ns event-sourcing-clj.domain.todo
  (:require [event-sourcing-clj.infra.aggregate :as agg]))

; -- value objects
(deftype TodoId [id])

; -- entities
(defrecord Todo [id text completed?]
  agg/Entity
  (id [_] (->TodoId id)))

; -- events
(defrecord Created [id text completed?])
(defrecord TextChanged [id new-text])
(defrecord CompletedChanged [id completed?])
(defrecord Deleted [id])
(defrecord DoneCleared [ids])

(defprotocol TodoCommands
  (create-new [_ id text])
  (change-text [_ id new-text])
  (set-completed [_ id completed?])
  (delete [_ id])
  (clear-done [_]) ; transactionally consistent... 1->many command example (tx boundary variant)
  )

(defprotocol TodoQueries
  (has-todo? [_ id])
  (all-todos [_])
  (get-todo [_ id])
  )

(declare my-accept)

(defrecord Todos [store]

  TodoQueries
  (has-todo? [model id]
    (contains? store id))
  (all-todos [model]
    (into #{} (vals store)))
  (get-todo [model id]
    (get store id))

  TodoCommands
  (create-new [model id text]
    (when-not (has-todo? model id)
      (->Created id text false)))
  (change-text [model id new-text]
    (when (has-todo? model id)
      (->TextChanged id new-text)))
  (set-completed [model id completed?]
    (when (has-todo? model id)
      (->CompletedChanged id completed?)))
  (delete [model id]
    (when (has-todo? model id)
      (->Deleted id)))
  (clear-done [model]
    (let [done-ids (->> (all-todos model)
                        (filter :completed?)
                        (map :id))
          done-ids (into #{} done-ids)]
      (->DoneCleared done-ids)))

  agg/Aggregate
  (accept [model event]
    (my-accept model event))
  )

(defmulti my-accept #(class %2))

(defmethod my-accept Created
  [model event]
  (let [id (:id event)
        todo (map->Todo event)]
    (assoc-in model [:store id] todo)))

(defmethod my-accept TextChanged
  [model event]
  (let [{:keys [id new-text]} event]
    (assoc-in model [:store id :text] new-text)))

(defmethod my-accept CompletedChanged
  [model event]
  (let [{:keys [id completed?]} event]
    (assoc-in model [:store id :completed?] completed?)))

(defmethod my-accept Deleted
  [model event]
  (let [id (:id event)]
    (update model :store dissoc id)))

(defmethod my-accept DoneCleared
  [model event]
  (let [ids (:ids event)]
    (update model :store #(apply dissoc % ids))))

(defn make-todos []
  (map->Todos {:store {}}))