(ns todos-www.app
  (:require [todos-www.core :refer [make-model]]
            [todos-www.ui :refer [main-ui]]
            [rum.core :as rum]))


(enable-console-print!)


(defonce app-state (atom (make-model)))

(defn main []
  (let [app-dom (.getElementById js/document "app")
        model @app-state
        app-ui (main-ui model)
        ]
    (rum/mount app-ui app-dom)))

;; ---

;; define your app data so that it doesn't get over-written on reload



(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (main)
  )


; ---

; https://pez.github.io/2016/03/01/Reagent-clientside-routing-with-Bidi-and-Accountant.html

(defn ^:export init! []

  (on-js-reload)
  )


(init!)