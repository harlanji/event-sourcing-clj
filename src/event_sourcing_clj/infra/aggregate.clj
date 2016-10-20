(ns event-sourcing-clj.infra.aggregate)


; -- infra

(defprotocol Aggregate
  (aggregate [model event]))

(defprotocol Entity
  (id [_]))

(defprotocol Proposer (propose [_ model]))
(defprotocol Acceptor (accept [_ model]))