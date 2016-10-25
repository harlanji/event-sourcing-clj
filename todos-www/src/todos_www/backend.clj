(ns todos-www.backend
  (:require [todos-www.core :refer [make-model]]
            [todos-www.ui :refer [main-ui layout-ui]]
            [rum.core :as rum]))


(defn main-html []
  (let [model (make-model)
        app-ui (main-ui model)]
    (rum/render-html (layout-ui app-ui))))


; note: this will not automaticallt reload because the figwheel plugin doesn't wrap-reload etc with its magic...
;       gotta fix in an upcoming commit where I integrate the project/ring structure--hence -rum suffix.
;       could pre-generate as a work-around...
(defn ring-handler [req]
  (println "ring handler")
  (when (= (:uri req) "/index-rum.html")
    {:body (main-html)
     :status 200
     :headers {"Content-Type" "text/html"}}))