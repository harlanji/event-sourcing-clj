(ns todos-www.app
  (:require [todos-www.core :refer [make-model]]
            [todos-www.ui :refer [main-ui]]
            [todos-www.routes :refer [routes]]
            [rum.core :as rum]
            [cljs.core.async :refer [<!] :as async]
            [cljs.reader :as reader]
            )
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))


(enable-console-print!)


(println (routes))

(defonce app-state (atom (make-model)))


(defn es-events
  "Handle events from an ES URL with EDN-read event types (kw is nice). Handler gets event triple [type data event]."
  [url handler-map]
  (let [es (new js/EventSource "/events")
        incoming-events (async/chan)
        on-event (fn [event]
                     (go (async/>! incoming-events
                                     [(reader/read-string (.-type event))
                                      (reader/read-string (.-data event))
                                      event])))]
    (doseq [[k v] handler-map]
      (if (keyword? k)
        (.addEventListener es k on-event)))
    (set! (.-onerror es)
          (fn [err]
            (.close es)
            (async/close! incoming-events)))
    (go-loop []
      (when-let [event (<! incoming-events)]
        (when-let [handler (get handler-map (first event))]
          (handler event))
        (recur)))))

(es-events "/events"
           {:message (fn [message] (println message))
            :coolness (fn [coolness] (println "coolness! " coolness))})

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