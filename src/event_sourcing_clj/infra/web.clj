(ns event-sourcing-clj.infra.web
  (:require [com.stuartsierra.component :as component]
            [org.httpkit.server :as hk]))

(defprotocol WebHandler
  (http-request! [_ req]))

(def error-404 {:body "404 :(" :status 404 :header {"Content-Type" "text/plain"}})

(defprotocol WebAppCommands
  (start-app [web-app new-app]))


(defrecord WebApp [opts
                   server
                   apps]

  WebAppCommands
  (start-app [_ new-app]
    (swap! apps conj new-app))

  WebHandler
  (http-request! [_ req]
    (let [response (some #(http-request! % req) @apps)]
      (or response error-404)))

  component/Lifecycle
  (start [app]
    (assoc app :server (hk/run-server #(http-request! app %) opts)))
  (stop [app]
    (update app :server nil)))
