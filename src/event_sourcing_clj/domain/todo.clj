(ns event-sourcing-clj.domain.todo
  (:require [event-sourcing-clj.infra.aggregate :refer [Aggregate]]))

; -- domain model + commands using defprotocol + defrecord for hinting

(defprotocol TodoCommands
  (create-new [_ id attrs])
  (modify [_ id attrs])
  ;(done [_ id])
  (delete [_ id]))

(defrecord Todo [id text completed?])


(defrecord TodosAggregate
  [todos]

  ; this would let us do FAST loads... other iface for transduce?
  ;clojure.core.protocols/CollReduce
  ;(coll-reduce [store f1 init] (coment "something with" accept))

  Aggregate
  (accept [store [evt opts]]
    ; note we do our own dispatch
    (cond
      (= evt ::todo-created)
      (let [todo opts]
        (update store :todos assoc (:id todo) todo))

      (= evt ::todo-modified)
      (let [changed-todo opts
            id (:id changed-todo)
            todos (update todos id merge changed-todo)]
        (assoc store :todos todos))

      (= evt ::todo-deleted)
      (let [id opts]
        (update store :todos dissoc id))))


  ; -- domain service
  ; pure + immutable -- no outside API calls, etc (side effects) -- all values provided by app svc


  ; event or nil returned for a command
  TodoCommands
  (create-new [_ id attrs]
    (when-not (contains? todos id)
      (let [todo (map->Todo (merge attrs {:id id}))]
        [::todo-created todo])))
  (modify [_ id attrs]
    (when (contains? todos id)
      (let [updates (merge attrs {:id id})]
        [::todo-modified updates])))
  (delete [_ id]
    (when (contains? todos id)
      [::todo-deleted id])))


(defn all-todos [todos]
  (into #{} (vals (:todos todos))))

(defn get-todo [todos id]
  ; fixme slow if first doesn't terminate (transducer style)
  (->> (all-todos todos)
       (filter #(= (:id %) id))
       (first)))


(defn make-todos []
  (map->TodosAggregate {:todos {}}))