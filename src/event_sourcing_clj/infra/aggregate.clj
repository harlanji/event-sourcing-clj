(ns event-sourcing-clj.infra.aggregate)


; -- infra

(defprotocol Aggregate
  (accept [_ [evt opts]]))


(defn cmd-of [state [cmd _]] cmd)

(defn app-atom-command
  "A convenience function to update an immutable service in an atom, and return the event (if any)."
  [app-svc-atom svc-fn & args]
  ; could make this transactional with fancy update function
  (when-let [event (apply svc-fn @app-svc-atom args)]
    (swap! app-svc-atom accept event)
    event))