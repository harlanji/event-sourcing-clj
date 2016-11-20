(ns todos-www.todos.routes
  (:require [todos-www.todos.core :refer [make-model]]
            [todos-www.todos.ui :refer [main-ui layout-ui]]
            [rum.core :as rum]))

(defn routes []
  (sorted-set
    ["/" :get `hello-world-handler]
    ["/index.html" :get `index-handler]))

(defn model-for-session [req]
  (make-model))

(defn main-html [req]
  (let [model (model-for-session req)
        app-ui (main-ui model)]
    (rum/render-html (layout-ui app-ui))))



(defn index-handler [req]
  {:status 200 :headers {"content-type" "text/html"} :body (main-html req)})

(defn hello-world-handler
  [request]
  {:status 200 :headers {} :body "Hello World"})




