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


(defprotocol )

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


Give building blocks as low level API

Give compiler that is high level and looks like CRUD/MVC but compiles down to low level API... and tools to migrate.


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




--


-


DDD Ubiquitous Language Parser


Model: Todos

Queries:

* has-todo?
* get-todo [id]
* done-todos
* all-todos

Actions:

* `create-new [id text]` accepted as `created [id text completed?]`
* `change-text [id new-text]` accepted as `text-changed [id new-text]`
* `change-completed [id completed?]` accepted as `completed-changed [id completed?]`
* `delete [id]` accepted as `deleted [id]`
* `clear-done []` accepted as `done-cleared [ids]`



```
(defprotocol TodoQueries
  (has-todo? [_ id])
  (all-todos [_])
  (get-todo [_ id])
  )

(defprotocol TodoCommands
  (create-new [_ id text])
  (change-text [_id new-text])
  (change-completed [_ id completed?])
  (delete [_ id])
  (clear-done [_]))
  
(defrecord Todos [store]
  TodoQueries
  
  TodoMutators ; rationalle, good for composing handlers across models. maybe too much work for no gain.
  (insert-todo [id text])
  (update [id attrs])
  (delete [_ id])
  
  
  TodoCommands
  (create-new [_ id text])
  (change-text [_id new-text])
  (change-completed [_ id completed?])
  (delete [_ id])
  (clear-done [_])
  
  Aggrete
  (propose [_ event])
  (accept [_ event])
  )
```

if we want to reuse parts then we can write it differently. maybe suggested patterns can be given for different types of use cases and transpilers can be provided.


-



Model, Command, Command Validator, Event, Mutator

Handlers = [Validator1(Queries1, Mutators1), Validator2(Queries2, Mutators2)]

Aggregate(Propose, Accept) = Projection(Handlers)

Event0 = Validate(Command0, Model0)

Model1 = Mutator(Model0, Event0)

Event1 = Validate(Command1, Model1)

Model2 = Mutator(Model1, Event1)

--


notes: validator needs read and write model... fair.



"normal" code looks like


Model1 = if(Validate(Model0,Event0)) then Mutate(Model0, Event0) else Model0

Model2 = if(Validate(Model1,Event1)) then Mutate(Model1, Event1) else Model1


--

in our model we add some extra pieces because we want to be able to share across consistency boundaries.

the problem is it's annoying to write code that way because you can't just follow it from top to bottom.

but if we were able to write our code and run it locally and then have it recompile to a distrubted pattern... that'd be awesome.

the hardest part is just getting people to cooperate and write code this way and entertain modeling questions in a format that they are not used to. 

people are used to ood questions but now we're asking ddd questions.



--

some people may want to build with editor completion, others may want to build with factories

can we let them code in either or both, and compile to either or both? >:]

eg. code in friendly style, compile to fast style (if they are opposite)

is adding or upgrading events at runtime affected? AOT would make a difference if classloaders aren't setup to handle it


--


Aggreate(Propose, Accept) = Model(Handlers.map(h => h(Write Model, Read Model)))

what is a projection?





-

we want to decompile these pieces by convention from code...



```

(defrecord Todo [store]

  (create-new [model id text] ; {:proposor true}  ?
    (with-meta
      (when-not (has-todo? id)
        (with-meta
          (let [todo (->Todo id text false)]
            (assoc-in model [:store id] todo))
          {:event ->Created} ; :params [id new-text] can be inferred from existing event. undefined event can generate it. derived values can be found from scope (macro or precompile required, precompile with macroexpand?)
          ; 1) replace (assoc-in) with (->Created id new-text)
          ; 2) generate (accept [model event] (let [todo (->Todo id text false)] (assoc-in model [:store id] todo))) for Todos
          ))
      {:validator true}))

  )
```

new idea: we can also materialize the code from way commands with an inverted parser.

idea: editor can suggest / hide meta tags. 



-

proposal: (->CreateTodo 1 "Do it!")

event: (->TodoCreated 1 "Do it!")

entity: (map->Todo {:id 1 :text "Do it!" :completed? false})

```
(defrecord CreateTodo [id text])

(defrecord Todo [id text completed?]
  Entity
  (id [_] (->TodoId id)))

(defrecord CreateTodo [id text]
  Proposer
  (^TodoCreated propose [_ model]
    (when-not (contains? model id)
      (->TodoCreated id text)))
  Mutator
  (accept [_ model ^TodoCreated event]
    (update assoc id (->Todo id text false))))
```

problem: mapping model. that's in, maybe the ->CreateTodo can be added with a ks path

-

How do you want to reuse your logic? single method interfaces and convenience factories for DSL

In a sense the function is the unit and these are all messing with delegation methods within the language


events have one transactionally consistent handler, N eventually consistent handlers



-

Mutate and Validate should happen on the same model, same r/w key paths

Apply and Propose -- seems like they're always together.



-

think in terms of what can happen to something, and then how that can happen. propose (negotiation), accept the event.

-


screw transactional boundaries, never think about consistency again. if it's coded for here it's consistent, if it's not it's not. part of your system is getting too hot to scale? break that component into a new microservice and put a queue between it... DONE

-


Coding is joyful and effortless if you cut it up right. Ever cooked with one of those knives that can cut atoms? That's what coding with my brain feels like

-

