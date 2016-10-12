(ns event-sourcing-clj.domain.notes
  (:require [event-sourcing-clj.infra.aggregate :as agg]))


; -- model

(defrecord Notes
  [entries])

; -- commands

(defn create-note [notes id date text]
  (when-not (contains? (:entries notes) id)
    [::note-created {:id id
                     :date date
                     :text text}]))


(defn delete-note [notes id]
  (when (contains? (:entries notes) id)
    [::note-deleted id]))


; -- aggregate

(defmulti accept agg/cmd-of)

(defmethod accept ::note-created
  [state [cmd opts]]
  (let [note opts
        id (:id note)]
    (update state :entries assoc id note)))

(defmethod accept ::note-deleted
  [state [cmd opts]]
  (let [id opts]
    (update state :entries dissoc id)))


(defn make-notes []
  (map->Notes {:entries {}}))