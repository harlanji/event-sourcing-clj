(ns event-sourcing-clj.domain.castore
  (:require [event-sourcing-clj.infra.aggregate :refer [Aggregate]]))

; -- domain model + commands using defprotocol + defrecord for hinting

(defprotocol CaStoreCommands
  (create-key [store pub-key])
  (sign-key [store pub-key])
  (revoke-key [store pub-key]))

(defrecord CaStore
  [keys]

  ; this would let us do FAST loads... other iface for transduce?
  ;clojure.core.protocols/CollReduce
  ;(coll-reduce [store f1 init] (coment "something with" accept))

  Aggregate
  (accept [store [evt opts]]
    ; note we do our own dispatch
    (cond
      (= evt ::key-created)
      (let [pub-key opts
            key-obj {:pub-key pub-key}]
        (update store :keys assoc pub-key key-obj))

      (= evt ::key-signed)
      (let [[pub-key sig] opts
            key-obj (assoc (get keys pub-key) :sig sig)]
        (update store :keys assoc pub-key key-obj))

      (= evt ::key-revoked)
      (let [pub-key opts]
        (update store :keys dissoc pub-key))))


  ; -- domain service
  ; pure + immutable -- no outside API calls, etc (side effects) -- all values provided by app svc


  ; event or nil returned for a command
  CaStoreCommands
  (create-key [_ pub-key]
    (when (not (contains? keys pub-key))
      (let [event [::key-created pub-key]]
        event)))
  (sign-key [_ pub-key]
    (when (contains? keys pub-key)
      (let [key-obj (get keys pub-key)
            event [::key-signed [pub-key "sig"]]]
        event)))
  (revoke-key [_ pub-key]
    (when (contains? keys pub-key)
      [::key-revoked pub-key])))



