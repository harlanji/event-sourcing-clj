(ns todos-www.app
  (:require [todos-www.core :refer [make-model app-routes]]
            [todos-www.ui :refer [main-ui]]
            [bidi.bidi :as bidi]
            [rum.core :as rum]))


(enable-console-print!)

(defn main []
  (let [app-dom (.getElementById js/document "app")
        ;route (bidi/match)
        model (make-model)
        app-ui (main-ui model)
        ]
    (rum/mount app-ui app-dom)))

;; ---

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
  (main)
  )


; ---

; https://pez.github.io/2016/03/01/Reagent-clientside-routing-with-Bidi-and-Accountant.html

(defn ^:export init! []

  (on-js-reload)
  )


(init!)