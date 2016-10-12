# Event Sourcing in Clojure

Implementing Event Sourcing + Aggregates to get a clear idea of how a system should be built.

Follows roughly the Appendix A of Vernon's Implementing DDD book.

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
