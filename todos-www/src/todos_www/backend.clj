(ns todos-www.backend
  (:require [clojure.core.async :as async]
            [todos-www.core :refer [make-model]]
            [todos-www.ui :refer [main-ui layout-ui]]
            [todos-www.routes :refer [routes]]

            [rum.core :as rum]
            [com.stuartsierra.component :as component]

            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :refer [body-params]]
            [io.pedestal.http.route :refer [query-params]]

            [figwheel-sidecar.system :as sys]
            [io.pedestal.http.sse :as sse]

            ))

(def sessions (atom {}))

(defn session-id [request]
  (get-in request [:query-params :session]))

(defn stream-ready [event-chan pedestal-context]
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

    (async/go-loop []
      (let [event (if (< 0.5 (rand))
                    {:name :message :data {:a 1 :b [:c 2]}}
                    {:name :coolness :data #{"alice" "bob"}})]
        (async/>!! event-chan event)
        (async/<! (async/timeout 1000))
        (recur)))))

(defn put-event [req]
  (let [session-id (session-id req)
        session (get @sessions session-id)
        in (:in session)
        event (:edn-params req)]
    (async/go (async/>! in event))
    {:status 200}))

(println "hi")

(defn with-sse [routes]
  (conj routes
        ["/events" :get [(body-params) query-params (sse/start-event-stream stream-ready)]]
        ["/events" :put [(body-params) query-params `put-event]]))

;(def common-interceptors [(body-params/body-params) http/html-body])

(defn main-html [req]
  (let [model (make-model)
        app-ui (main-ui model)]
    (rum/render-html (layout-ui app-ui))))

(defn index-handler [req]
  {:status 200 :headers {"content-type" "text/html"} :body (main-html req)})

(defn hello-world-handler
  [request]
  {:status 200 :headers {} :body "Hello World"})

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


(defn system []
  (component/system-map
    :figwheel (sys/figwheel-system (sys/fetch-config))
    :http-server (pedestal-server (service (with-sse (routes))))))


(defn -main [& args]
  (component/start (system)))
