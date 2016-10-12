# Event Sourcing in Clojure

Implementing Event Sourcing + Aggregates to get a clear idea of how a system should be built.

Follows roughly the Appendix A of Vernon's Implementing DDD book.

## Status

The `todo` model is the best thing to look at. Things can be learned from the commit history as well.

Aggregate API could use some work, but I think the ideas are solid.

Next steps may be to bolt a UI on top.

## Packages

* `infra.*` ES + A scaffolding protocols, records
* `domain.*` Pure in-memory representation of aggregate DS + commands + handlers
* `app.*` Application-level representation of domain model--in memory in our case.
* `core` The application.

## Ideas


Aggregate = processors + view projection in a transactional boundry, first processor wins (one event per command)


## Design Notes

* projection = protocol(s) with pure query functions
* processor = agg/Aggregate handling with `(accept [state [evt opts]])` dispatching on `evt` (can we do better?)
  * relative to self view projection object in defrecord (transactional consistentcy expressed via fields--inconvient update return)
  * the notes aggregate shows defmulti based accept dispatch
* currently no validation messages, simply nil returned. could emit errors on a side channel or inline with a reserved ns

Continues / zooms in on some earlier work: [`harlanji/clojureseed`](https://github.com/harlanji/clojureseed)

### Clojurism

You write the descriptions of these things in pure functions to a familiar / standard API
and then the build or runtime system can intelligently recompose the logic to run with scale.

Datatypes and construction of expressions can communicate intent like body language. From this we 
can infer transactional boundaries and possibly other things for recomposition.

It uses monitoring to optimize runtime conditions, like an online query planner + optimizer.

`Compose(Config, Code, Data, Metrics)`