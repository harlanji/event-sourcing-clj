(ns event-sourcing-clj.app.notes
  (:require [event-sourcing-clj.domain.notes :as notes]))

(defprotocol NotesAppCommands
  (create-note [_ text])
  (delete-note [_ id]))

(defrecord NotesApp
  [notes clock-fn]

  NotesAppCommands
  (create-note [_ text]
    (let [date (clock-fn)
          new-id (str "id." date)]
      (when-let [event (notes/create-note @notes new-id date text)]
        (swap! notes notes/accept event)
        event)))
  (delete-note [_ id]
    (when-let [event (notes/delete-note @notes id)]
      (swap! notes notes/accept event)
      event))

  )


(defn make-notes-service
  []
  (map->NotesApp {:clock-fn #(str "time-" (Math/random))
                  :notes (atom (notes/make-notes))}))