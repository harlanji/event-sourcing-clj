(ns todos-www.backend
  (:require [todos-www.todos.routes :refer [routes]]
            [todos-www.backend.events :refer [with-sse]]

            [com.stuartsierra.component :as component]

            [io.pedestal.http :as http]

            [figwheel-sidecar.system :as sys]

            ))


(println "hi")


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
