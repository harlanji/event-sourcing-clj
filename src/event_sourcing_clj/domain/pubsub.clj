(ns event-sourcing-clj.domain.pubsub
  (:require [event-sourcing-clj.infra.aggregate :as agg]))


(defprotocol PubsubCommands
  (create-subscription [_ channel])
  (publish [_ channel message]))


(defrecord Pubsub
  [subscriptions
   receipts
   publish!]

  ;
  ; design note: we can build list of subscribers in model + event,
  ;              or consume it in the aggregate (augment with optional delivery event? seems gross)
  ; if we have versioning in the event then we can be sure we're consistent in aggregate.
  ; doing it with/without could affect partitionability. huge subscriber count... explicit/implicit, latter wins (and is AP compat).
  PubsubCommands
  (create-subscription [_ sub-id]
    (when-not (contains? subscriptions sub-id)
      (let [event [::subscription-created {:id sub-id}]]
        event)))

  (cancel-subscription [_ sub-id]
    (when (contains? subscriptions sub-id)
      [::subscription-cancelled sub-id]))

  (publish [_ payload]
    (let [event [::message-published {:payload payload}]]
      event))

  agg/Aggregate
  (accept [state [evt opts]]
    (cond
      (= evt ::subscription-created)
      (let [sub opts
            sub-id (:id sub)]
        (update state :subscriptions assoc sub-id sub))

      (= evt ::subscription-cancelled)
      (let [sub-id opts]
        (update state :subscriptions dissoc sub-id))

      (= evt ::message-published)
      (let [{:keys [payload]} opts
            ; publish! should be in a different consistent receiver of ::message-published, not here
            receipt (publish! subscriptions payload)]
        (update state :receipts conj receipt))

      (= evt ::message-delivered)
      nil

      :default nil))

  )

(defn make-pubsub-service [publish!]
  (map->Pubsub {:subscriptions {}
                :receipts []
                :publish! publish!}))