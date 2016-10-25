(ns todos-www.backend
  (:require [todos-www.core :refer :all]
            [rum.core :as rum]))


(defn main-html []
  (let [app-ui (main-ui)]
    (rum/render-html (main-template app-ui))))


; note: this will not automaticallt reload because the figwheel plugin doesn't wrap-reload etc with its magic...
;       gotta fix in an upcoming commit where I integrate the project/ring structure--hence -rum suffix.
;       could pre-generate as a work-around...
(defn ring-handler [req]
  (println "ring handler")
  (when (= (:uri req) "/index-rum.html")
    {:body (main-html)
     :status 200
     :headers {"Content-Type" "text/html"}}))