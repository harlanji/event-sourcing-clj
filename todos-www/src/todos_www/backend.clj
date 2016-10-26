(ns todos-www.backend
  (:require [todos-www.core :refer [make-model app-routes]]
            [todos-www.ui :refer [main-ui layout-ui]]
            [bidi.bidi :as bidi]
            [rum.core :as rum]))



(defn main-html [req]
  (let [model (make-model)
        app-ui (main-ui model)]
    (rum/render-html (layout-ui app-ui))))


; note: this will not automaticallt reload because the figwheel plugin doesn't wrap-reload etc with its magic...
;       gotta fix in an upcoming commit where I integrate the project/ring structure--hence -rum suffix.
;       could pre-generate as a work-around...
(defn ring-handler [req]
  (let [route (bidi/match-route app-routes (:uri req))]
    (when (= :index (:handler route))
      {:body (main-html req)
       :status 200
       :headers {"Content-Type" "text/html"}})))