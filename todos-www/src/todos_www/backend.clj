(ns todos-www.backend
  (:require [clojure.core.async :as async]
            [todos-www.todos.core :as todos]
            [todos-www.todos.routes :refer [routes]]

            [com.stuartsierra.component :as component]

            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :refer [body-params] :as bp]
            [io.pedestal.http.route :refer [query-params]]

            [figwheel-sidecar.system :as sys]
            [io.pedestal.http.sse :as sse]

            ))

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

(println "hi")

(defn with-sse [routes sessions]

  ; todo use tagged for records
  ;(let [parsers (bp/default-parser-map {:edn-options {}})])

  (conj routes
        ["/events" :put [(body-params) query-params (partial put-event sessions)] :route-name :events]
        ["/events/sse" :get [(body-params) query-params (sse/start-event-stream (partial stream-ready sessions))] :route-name :stream-ready]
        ))

;(def common-interceptors [(body-params/body-params) http/html-body])


(defn service [routes]
  {:env :prod
   ;; You can bring your own non-default interceptors. Make
   ;; sure you include routing and set it up right for
   ;; dev-mode. If you do, many other keys for configuring
   ;; default interceptors will be ignored.
   ;; ::http/interceptors []
   ::http/routes routes

   ;; Uncomment next line to enable CORS support, add
   ;; string(s) specifying scheme, host and port for
   ;; allowed source(s):
   ;;
   ;; "http://localhost:8080"
   ;;
   ;;::http/allowed-origins ["scheme://host:port"]

   ;; Root for resource interceptor that is available by default.
   ::http/resource-path "/public"

   ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
   ::http/type :jetty
   ;;::http/host "localhost"
   ::http/port 8080
   ;; Options to pass to the container (Jetty)
   ::http/container-options {:h2c? true
                             :h2? false
                             ;:keystore "test/hp/keystore.jks"
                             ;:key-password "password"
                             ;:ssl-port 8443
                             :ssl? false}})


(defrecord PedestalServer [service server]
  component/Lifecycle
  (start [c]
    (assoc c :server (http/start (http/create-server service))))
  (stop [c]))

(defn pedestal-server [service]
  (map->PedestalServer {:service service}))


(defn system [sessions]
  (component/system-map
    :figwheel (sys/figwheel-system (sys/fetch-config))
    :http-server (pedestal-server (service (with-sse (routes) sessions)))))

(defonce sessions (atom {}))

(defn -main [& args]
  (component/start (system sessions)))
