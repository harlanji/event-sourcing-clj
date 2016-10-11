(ns event-sourcing-clj.core
  (:require [event-sourcing-clj.app.castore :as castore-app]
            [event-sourcing-clj.app.notes :as notes-app]
            [event-sourcing-clj.app.todo :as todo-app]))
; application service accepts command, has reference to repo and aggregate


(def castore (castore-app/make-castore-service))
(def notes (notes-app/make-notes-service))
(def todos (todo-app/todo-service))


(defn load-sample-data []
  (castore-app/create-key castore "A")
  (castore-app/create-key castore "B")
  (castore-app/create-key castore "C")
  (castore-app/create-key castore "D")

  (castore-app/sign-key castore "B")
  (castore-app/revoke-key castore "C")

  (notes-app/create-note notes "Hello hello!")
  (notes-app/create-note notes "ohaio!")

  (todo-app/create-todo todos "Do a thing")
  (todo-app/create-todo todos "And another one")

  nil)



(defn -main
  [& args]
  (load-sample-data)
  (println castore)
  (println notes)
  (println todos))