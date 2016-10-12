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


(defmulti crud-processor cmd-of)

(defmethod crud-processor :crud/created [todos [evt opts]]
  (let [todo opts]
    (update todos :store assoc (:id todo) todo)))

(defmethod crud-processor :crud/modified [todos [evt opts]]
  (let [changed-todo opts
        id (:id changed-todo)]
    (update todos :store update id merge changed-todo)))

(defmethod crud-processor :crud/deleted [todos [evt opts]]
  (let [id opts]
    (update todos :store dissoc id)))

(defmethod crud-processor :crud/many-deleted [todos [evt opts]]
  (let [ids opts
        events (map (fn [id] [:crud/deleted id]) ids)
        todos (reduce accept todos events)]
    todos))