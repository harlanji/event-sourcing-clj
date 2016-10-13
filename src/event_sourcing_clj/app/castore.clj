(ns event-sourcing-clj.app.castore
  (:require [event-sourcing-clj.domain.castore :as castore]
            [event-sourcing-clj.infra.aggregate :as agg]
            ))

(defprotocol CaStoreApp
  (create-key [_ pub-key])
  (sign-key [_ pub-key])
  (revoke-key [_ pub-key]))

; responsible for accepting commands and transactions
; and emiting events. eg cross-aggregate transactions can use refs.
(defrecord CaStoreAppService
  [castore]

  CaStoreApp
  (create-key [_ pub-key]
    (when-let [event (castore/create-key @castore pub-key)]
      (swap! castore agg/aggregate event)
      event))
  (sign-key [_ pub-key]
    (when-let [event (castore/sign-key @castore pub-key)]
      (swap! castore agg/aggregate event)
      event))
  (revoke-key [_ pub-key]
    (when-let [event (castore/revoke-key @castore pub-key)]
      (swap! castore agg/aggregate event)
      event)))

; idea: macro for generic single domain service with atom repo, as well as core.async + kv store


(defn make-castore-service []
  (let [castore (atom (castore/map->CaStore {}))]
    (map->CaStoreAppService {:castore castore})))