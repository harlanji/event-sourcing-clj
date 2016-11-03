(ns todos-www.core
  (:require [clojure.pprint :refer [pprint]]))

(defn date-seq [m from to]
  (map (fn [%] {:m m :d %}) (range from to)))

(defn tasks-for-date [d]
  (if (even? (:d d))
    [{:name (str "Todo " (:d d))}]
    []))

(defn with-tasks [date-seq]
  (map #(assoc % :tasks (tasks-for-date %)) date-seq))

(defn make-model []
  (-> (date-seq 1 1 31)
      with-tasks))


; ---

(defmulti propose (fn [model proposal] (:proposal proposal)))
(defmulti accept (fn [model event] (first event)))


(defprotocol KvRead
  (get-value [_ k])
  (all-keys [_]))

(defprotocol KvWrite
  (put-value [_ k v])
  (delete [_ k]))

(defrecord KvModel [kv]
  KvRead
  ; can use built-in record fields access model
  (get-value [_ k]
    (get kv k))
  (all-keys [_]
    (keys kv))

  KvWrite
  ; must return mutated copy of model
  (put-value [model k v]
    (update model :kv assoc k v))
  (delete [model k])

  )

; request - invocation (future, network)
(defrecord create-key [k v])

; event - application (past, fact)
(defrecord key-created [k v])


; proposal - validate and return event (no write)
(defmethod propose ::create-key [model proposal]
  (let [k (:k proposal)
        v (:v proposal)]
    (when-not (contains? (:kv model) k)
      (->key-created k v))))


; acceptance - write
(defmethod accept ::key-created [model event]
  (let [{:keys [k v]} event]
    (put-value model k v)))





(println "class: " (type (->create-key :a :b)))



; --



(defn propose_ [req]
  (let []))

(defn state [req]
  )

(defn sse-events [req]
  )

(defn model-handler []
  (let [routes #{["/propose" :put `propose_]
                 ["/state" :get `state]
                 ["/events" :get `sse-events]}]))