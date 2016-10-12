# Event Sourcing in Clojure

Implementing Event Sourcing + Aggregates to get a clear idea of how a system should be built.

Follows roughly the Appendix A of Vernon's Implementing DDD book.

## Status

The `todo` model is the best thing to look at. Things can be learned from the commit history as well.

Aggregate API could use some work, but I think the ideas are solid.

Next steps may be to bolt a UI on top.

This README is really messy... lots of thoughts to organize.

*This repo is best understood via commit history*

## Packages

* `infra.*` ES + A scaffolding protocols, records
* `domain.*` Pure in-memory representation of aggregate DS + commands + handlers
* `app.*` Application-level representation of domain model--in memory in our case.
* `core` The application.

## Ideas


Aggregate = processors + view projection in a transactional boundry, first processor wins (one event per command)


```

; value objects
(deftype TodoId [id])

; entities
(defrecord Todo [id text completed?]
  agg/Entity
  (id [_] (->TodoId id)))

; events
(defrecord Created [id text completed?])
(defrecord TextChanged [id new-text])
(defrecord CompletedChanged [id completed?])
(defrecord DoneCleared [ids])

(defprotocol TodoCommands
  (create-new [_ id text])
  (change-text [_ id new-text])
  (change-completed [_ id completed?])
  (clear-done [_]))

(defprotocol TodoQueries
  (get-todo [_ id])
  (all-todos [_])
  (done-todos [_]))

(defrecord Todos
  [store]
  
  TodoCommands
  (create-new [model id text]
    (update model :store assoc id (->Todo id text false)))

  TodoQueries
  (get-todo [model id] (get store id))
  (all-todos [model]
    (into #{} (vals store)))

  agg/Aggregate
  (accept [model event]
    (cond
      (= (class event) Created)
      (let [id (:id event)
            text (:text event)
            new-todo (->Todo id text false)]
        (update model :store assoc id new-todo))))

```

; value objects (generally for external references)...
; we SHOULD use this, but it's pretty non-clojure-idiomatic.
; maybe instead VOs of primitives can be a tag? Or act as primitives!

```
(defprotocol Entity
  (id "Get ID for reference by external contexts." [_]))

  TodoQueries
  (all-todos [model]
    (into #{}
          (map (fn [todo]
                 (update todo :id ->TodoId))
               (vals store))))
```




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


```

```


## Design Notes

* projection = protocol(s) with pure query functions
* processor = agg/Aggregate handling with `(accept [state [evt opts]])` dispatching on `evt` (can we do better?)
  * relative to self view projection object in defrecord (transactional consistentcy expressed via fields--inconvient update return)
  * the notes aggregate shows defmulti based accept dispatch
* currently no validation messages, simply nil returned. could emit errors on a side channel or inline with a reserved ns

Continues / zooms in on some earlier work: [`harlanji/clojureseed`](https://github.com/harlanji/clojureseed)


## Architecture

The approach is heavily influenced by Rails. We want a small and complete set of ideas that can be understood easily.

Priority 1 is new developers, much like rails.

Priority 2 is gaining adoptions by replacing Rails systems.

Drop-in is best. Open source is preferred. Colloquial terms preferred, but always with clarification when desired.

Avoid re-using terms.

"Materialized View" is hard because people think view means HTML. 


Materialized table.

Do concepts need to have fully encompassing functions or data structures? Is it annoying to acheive that through composition? Does that cause people to repeat themselves? Does it require tool support to catch "misspelled variables" (I used to joke about unit testing for spelling... strong typing fan).


*Command validation* and *event transformation* depend on a *set of queries* that work against a *model*. 

Aggregates *accept events* and *apply* them to the model.

* Validation and transformation happen in one step in the implementation of the command method.
* The set of queries is defined on the domain service with defprotocols and realized with defrecord.
* The aggregate accepts events through a reduction-compatible interface in a method called accept, where they are applied.


A model composes queries in a transactional unit (bounded context) which may also be an aggregate.

An aggregate should not use eventually consistent views when determining transactions (why? true?).

(may explicitly use? good to call it out).

How do we realize app vs domain service? 

Can we use pluggable backends as long as they're transactional? Like Avout impls and clojure xa via immutant


(side note: have I been ignorant to this the whole time? look back at Tx descriptions in old EE/Spring books).



## Usage

1. List all "aggregates" (aggregates are singleton)

2. List all commands handled by an aggregate

3. List all routes and the html files that the uze

(router is an aggregate)

4. Compose handlers and projections into aggregates



### Clojurism

You write the descriptions of these things in pure functions to a familiar / standard API
and then the build or runtime system can intelligently recompose the logic to run with scale.

Datatypes and construction of expressions can communicate intent like body language. From this we 
can infer transactional boundaries and possibly other things for recomposition.

It uses monitoring to optimize runtime conditions, like an online query planner + optimizer.

`Compose(Config, Code, Data, Metrics)`

### Can we "migrate" a running aggregate?

Yes.

1. Pause processing
2. Transform current model to new model (using migration-defined function)
3. Create new aggregate with transformed model
4. Resume processing on new instance

### How to we integrate with existing solutions?

1. Java
2. Rails
3. PHP
4. Node


Node thinks they are awesome so we want to make them want to do what Java and Rails are doing.

PHP is strictly business, might want to attract new engineers (or they like the ones they are attracting)

They'll adopt a good idea if it's written in PHP.

-

If your order at a coffee shop starts with "soy decaf" I might chuckle a little and imagine that we can never be friends. Only a joke, kinda.
