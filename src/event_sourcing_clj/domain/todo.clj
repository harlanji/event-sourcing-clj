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

; queries
(defprotocol TodoQueries
  (has-todo? [_ id])
  (all-todos [_])
  (get-todo [_ id])
  )

; read model / projection
(defrecord Todos [store]
  TodoQueries
  (has-todo? [model id]
    (contains? store id))
  (all-todos [model]
    (into #{} (vals store)))
  (get-todo [model id]
    (get store id)))


; -- validate + accept pairs
(defmulti my-accept #(class %2))
(extend-type Todos
  agg/Aggregate
  (accept [model event]
    (my-accept model event)))


; -- create a new todo

(defrecord CreateNew [id
                      text]
  agg/Command
  (valid? [_ model]
    (when-not (has-todo? model id)
      (->Created id text false))))

(defmethod my-accept Created
  [model event]
  (let [id (:id event)
        todo (map->Todo event)]
    (assoc-in model [:store id] todo)))

; -- change text of a todo

(defrecord ChangeText [id
                       new-text]
  agg/Command
  (valid? [_ model]
    (when (has-todo? model id)
      (->TextChanged id new-text))))


(defmethod my-accept TextChanged
  [model event]
  (let [{:keys [id new-text]} event]
    (assoc-in model [:store id :text] new-text)))

; -- change completed status of a todo

(defrecord ChangeCompleted [id
                            completed?]
  agg/Command
  (valid? [_ model]
    (when (has-todo? model id)
      (->CompletedChanged id completed?))))


(defmethod my-accept CompletedChanged
  [model event]
  (let [{:keys [id completed?]} event]
    (assoc-in model [:store id :completed?] completed?)))


; -- delete a todo

(defrecord Delete [id]
  agg/Command
  (valid? [_ model]
    (when (has-todo? model id)
      (->Deleted id))))

(defmethod my-accept Deleted
  [model event]
  (let [id (:id event)]
    (update model :store dissoc id)))


; -- clear done todos

(defrecord ClearDone []
  agg/Command
  (valid? [_ model]
    (let [done-ids (->> (all-todos model)
                        (filter :completed?)
                        (map :id))
          done-ids (into #{} done-ids)]
      (->DoneCleared done-ids))))


(defmethod my-accept DoneCleared
  [model event]
  (let [ids (:ids event)]
    (update model :store #(apply dissoc % ids))))

; -- make a service

(defn make-todos []
  (map->Todos {:store {}}))