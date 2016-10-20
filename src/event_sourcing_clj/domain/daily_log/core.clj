(ns event-sourcing-clj.domain.daily-log.core
  (:require [clojure.string :as str]
            [event-sourcing-clj.infra.aggregate
             :refer [Proposer propose
                     Acceptor accept
                     Entity id]
             :as agg]))

; -- note (entity)

(deftype NoteId [note-id])

(defrecord Note [note-id date]
  Entity
  (id [_] (->NoteId note-id)))

; -- entry (entity)

(deftype EntryId [entry-id])

(defrecord Entry [note-id entry-id content timestamp]
  Entity
  (id [_] (->EntryId note-id)))

; -- note read

(def ^:const entry-separator #"(?mux) \n-\n")

(defprotocol Read
  (contains-note? [_ note-id])
  (get-notes [_])
  (get-note [_ note-id])
  (entries-for-note [model note-id]))

; -- note write

(defprotocol Write
  (create-note [model note-id date])
  (append-entry [model note-id entry-id content timestamp])) ; should be append entry

; -- create note

(defrecord NoteCreated [note-id entry-id content timestamp]
  Acceptor
  (accept [_ model]
    (append-entry model note-id entry-id content timestamp)))


(defrecord CreateNote [note-id entry-id content timestamp]
  Proposer
  (propose [_ model]
    (when-not (contains-note? model note-id)
      (->NoteCreated note-id entry-id content timestamp))))