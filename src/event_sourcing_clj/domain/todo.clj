(ns event-sourcing-clj.domain.todo
  (:require [event-sourcing-clj.infra.aggregate :as agg]))

; -- domain model + commands using defprotocol + defrecord for hinting

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
  (get-todo [_ id]))

(defrecord Todo [id text completed?])

(declare modify)

; A) shared with view projection
;    same as B, except within defrecord iteself.
;    main advantage is shorthand for view fields (transactional consistent).
; B) one aggregate per app (per module)
;(extend-type TodosApp
;  agg/Aggregate
;  (accept [store [evt opts]]))
;(extend-type NotesApp
;  agg/Aggregate
;  (accept [store [evt opts]]))
;(extend-type NotesApp
;  agg/Aggregate
;  (accept [store [evt opts]]))

; C) all aggregates in one place (central module)
;(extend-protocol agg/Aggregate
;  TodosApp
;  (accept [store [evt opts]])
;  NotesApp
;  (accept [store [evt opts]])
;  CaStoreApp
;  (accept [store [evt opts]]))


(defrecord Todos
  [store]

  ; this would let us do FAST loads... other iface for transduce?
  ;clojure.core.protocols/CollReduce
  ;(coll-reduce [store f1 init] (coment "something with" accept))

  agg/Aggregate
  (accept [todos [evt opts]]
    ; note we do our own dispatch
    (cond
      (= evt :crud/created)
      (let [todo opts]
        (update todos :store assoc (:id todo) todo))

      (= evt :crud/modified)
      (let [changed-todo opts
            id (:id changed-todo)]
        (update todos :store update id merge changed-todo))

      (= evt :crud/deleted)
      (let [id opts]
        (update todos :store dissoc id))

      (= evt :crud/many-deleted)
      (let [ids opts
            events (map (fn [id] [:crud/deleted id]) ids)
            todos (reduce agg/accept todos events)]
        todos)


      ))

  ; -- domain service
  ; pure + immutable -- no outside API calls, etc (side effects) -- all values provided by app svc


  ; event or nil returned for a command
  TodoCommands
  (create-new [todos id text]
    (when-not (has-todo? todos id)
      (let [todo (map->Todo {:id id
                             :text text
                             :completed? false})]
        [:crud/created todo])))

  (change-text [todos id new-text]
    (modify todos id {:text new-text}))

  (set-completed [todos id completed?]
    (modify todos id {:completed? completed?}))

  (delete [todos id]
    (when (has-todo? todos id)
      [:crud/deleted id]))

  (clear-done [todos]
    ; a commnd without explicit intent... ie. we commit to the results of a query.
    ; seems we'd want to query and then delete (delete-many % (map :id (get-done %)) in some way.
    ; otoh this conveys transactional intent.
    (let [done-ids (into #{} (->> (all-todos todos)
                                  (filter :completed?)
                                  (map :id)))]
      [:crud/many-deleted done-ids]))


  TodoQueries
  (has-todo? [_ id]
    (contains? store id))

  (all-todos [_]
    (into #{} (vals store)))

  (get-todo [_ id]
    (get store id)))



(defn- modify [todo-queries id attrs]
  (when (has-todo? todo-queries id)
    ; this map->Todo thing is a hack... need to infer type of thing without having it
    (let [updates (merge (map->Todo {}) attrs {:id id})]
      [:crud/modified updates])))

(defn make-todos []
  (map->Todos {:store {}}))