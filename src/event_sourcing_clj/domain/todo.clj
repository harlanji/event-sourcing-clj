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

(declare modify)


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
      (let [todo (map->Todo {:id id
                             :text text
                             :completed? false})]
        [:crud/created todo])))
  (change-text [model id new-text]
    (modify model id {:text new-text}))
  (set-completed [model id completed?]
    (modify model id {:completed? completed?}))
  (delete [model id]
    (when (has-todo? model id)
      [:crud/deleted id]))
  (clear-done [model]
    (let [done-ids (->> (all-todos model)
                        (filter :completed?)
                        (map :id))
          done-ids (into #{} done-ids)]
      [:crud/many-deleted done-ids]))

  agg/Aggregate
  (accept [model [evt opts]]
    (agg/crud-processor model [evt opts]))
  )

(defn- modify
  "Not quite able to be pulled into aggregate package because of has-todo? could generalize crud queries."
  [model id attrs]
  (when (has-todo? model id)
    ; this map->Todo thing is a hack... need to infer type of thing without having it
    (let [updates (merge (map->Todo {}) attrs {:id id})]
      [:crud/modified updates])))

(defn make-todos []
  (map->Todos {:store {}}))