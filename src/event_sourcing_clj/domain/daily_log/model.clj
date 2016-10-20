(ns event-sourcing-clj.domain.daily-log.model
  (:require [event-sourcing-clj.domain.daily-log.core :refer :all]
            [event-sourcing-clj.domain.daily-log.core :refer :all]
            [event-sourcing-clj.infra.aggregate :refer [id]]
            [clojure.string :as str]))

(defrecord DailyLog [notes idea-id-factory]

  Read
  (contains-note? [_ note-id]
    (contains? notes note-id))
  (get-notes [_]
    (keys notes))
  (get-note [_ note-id]
    (get notes note-id))
  (entries-for-note [model note-id]
    (let [note (get-note model note-id)]
      (when note
        note)))



  Write
  (create-note [model note-id date]
    (let [note (map->Note {:note-id note-id
                           :date date
                           :entries []})
          note []]
      (assoc-in model [:notes note-id] note)))

  (append-entry [model note-id entry-id content timestamp]
    (let [entry (map->Entry {:note-id note-id
                             :entry-id entry-id
                             :content content
                             :timestamp timestamp})]
      (update-in model [:notes note-id] conj entry)))

  #_ IdeasRead
  #_ (ideas-for-note [model note-id]
    (let [entries (entries-for-note model note-id)
          ideas (mapcat #(extract-ideas model idea-id-factory %) entries)]
      ideas))

  )