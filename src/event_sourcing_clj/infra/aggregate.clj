(ns event-sourcing-clj.infra.aggregate)


; -- infra

(defprotocol Aggregate
  (aggregate [model event]))

(defprotocol Entity
  (id [_]))

(defprotocol Proposer (propose [_ model]))
(defprotocol Acceptor (accept [_ model]))

(defn aggregate!
  "A convenience function to update an immutable service in an atom, and return the event (if any)."
  [swap! app-svc-atom svc-fn & args]
  ; could make this transactional with fancy update function
  (when-let [event (apply svc-fn @app-svc-atom args)]
    (println "aggregate:" event)
    (swap! app-svc-atom aggregate event)
    event))