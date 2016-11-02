(ns todos-www.backend
  (:require [todos-www.core :refer [make-model]]
            [todos-www.ui :refer [main-ui layout-ui]]
            [todos-www.routes :refer :all]

            [rum.core :as rum]
            [com.stuartsierra.component :as component]

            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]

            [figwheel-sidecar.system :as sys]
            ))


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

(defn routes []
  #{["/" :get `hello-world-handler]
    ["/index.html" :get `index-handler]})

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
    :http-server (pedestal-server (service (routes)))))


(defn -main [& args]
  (component/start (system)))
