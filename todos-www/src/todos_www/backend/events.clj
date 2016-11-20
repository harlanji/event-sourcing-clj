(ns todos-www.backend.events
  (:require [clojure.core.async :as async]
            [todos-www.todos.core :as todos]
            [io.pedestal.http.body-params :refer [body-params]]
            [io.pedestal.http.route :refer [query-params]]
            [io.pedestal.http.sse :as sse]))


(defn session-id [request]
  (get-in request [:query-params :session]))

(defn stream-ready [sessions event-chan pedestal-context]
  (let [{:keys [request]} pedestal-context
        session-id (session-id request)
        in (async/chan)
        session {:out event-chan :in in}]

    (println "started session" session-id)

    (swap! sessions assoc session-id session)

    ; incoming
    (async/go-loop []
                   (when-let [event (async/<! in)]
                     (println "received event in session" session-id ": " event)
                     (recur)))

    (async/>!! event-chan {:name ::todos/key-created
                           :data (todos/->key-created :k :v)})


    #_ (async/go-loop []
                      (let [event (if (< 0.5 (rand))
                                    {:name :message :data {:a 1 :b [:c 2]}}
                                    {:name :coolness :data #{"alice" "bob"}})]
                        (async/>!! event-chan event)
                        (async/<! (async/timeout 1000))
                        (recur)))
    nil))

(defn put-event [sessions req]
  (let [session-id (session-id req)
        session (get @sessions session-id)
        in (:in session)
        event (:edn-params req)]
    (println "event!" event)
    (async/go (async/>! in event))
    {:status 200}))


(defn with-sse [routes sessions]

  ; todo use tagged for records
  ;(let [parsers (bp/default-parser-map {:edn-options {}})])

  (conj routes
        ["/events" :put [(body-params) query-params (partial put-event sessions)] :route-name :events]
        ["/events/sse" :get [(body-params) query-params (sse/start-event-stream (partial stream-ready sessions))] :route-name :stream-ready]
        ))