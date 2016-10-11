(ns event-sourcing-clj.domain.todo
  (:require [event-sourcing-clj.infra.aggregate :refer [Aggregate]]))

; -- domain model + commands using defprotocol + defrecord for hinting

(defprotocol TodoCommands
  (create-new [_ id text])
  (change-text [_ id new-text])
  (set-completed [_ id completed?])
  (delete [_ id])
  (clear-done [_]) ; transactionally consistent... 1->many command example (tx boundary variant)
  )

(defprotocol TodoQueries
  (all-todos [_])
  (get-todo [_ id]))

(defrecord Todo [id text completed?])

(declare modify)

(defrecord TodosAggregate
  [todos]

  ; this would let us do FAST loads... other iface for transduce?
  ;clojure.core.protocols/CollReduce
  ;(coll-reduce [store f1 init] (coment "something with" accept))

  Aggregate
  (accept [store [evt opts]]
    ; note we do our own dispatch
    (cond
      (= evt :crud/created)
      (let [todo opts]
        (update store :todos assoc (:id todo) todo))

      (= evt :crud/modified)
      (let [changed-todo opts
            id (:id changed-todo)
            todos (update todos id merge changed-todo)]
        (assoc store :todos todos))

      (= evt :crud/deleted)
      (let [id opts]
        (update store :todos dissoc id))))


  ; -- domain service
  ; pure + immutable -- no outside API calls, etc (side effects) -- all values provided by app svc


  ; event or nil returned for a command
  TodoCommands
  (create-new [_ id text]
    (when-not (contains? todos id)
      (let [todo (map->Todo {:id id
                             :text text
                             :completed? false})]
        [:crud/created todo])))

  (change-text [_ id new-text]
    (modify todos id {:text new-text}))

  (set-completed [_ id completed?]
    (modify todos id {:completed? completed?}))

  (delete [_ id]
    (when (contains? todos id)
      [:crud/deleted id]))


  TodoQueries
  (all-todos [_]
    (into #{} (vals todos)))

  (get-todo [this id]
    ; fixme slow if first doesn't terminate (transducer style)
    (->> (all-todos this) ; this is clunky (state vs todos... ambiguous to reader)
         (filter #(= (:id %) id))
         (first)))

  )



(defn- modify [todos id attrs]
  (when (contains? todos id)
    ; this map->Todo thing is a hack... need to infer type of thing without having it
    (let [updates (merge (map->Todo {}) attrs {:id id})]
      [:crud/modified updates])))

(defn make-todos []
  (map->TodosAggregate {:todos {}}))