(ns event-sourcing-clj.app.daily-log
  (:require [event-sourcing-clj.domain.dail-log.core :refer :all]
            [event-sourcing-clj.infra.aggregate :as agg]))


#_ (->> (str/split (:content note)
                entry-separator)
     #_ (map str/trim)
     #_ (map #(map->Entry {:note-id (id note)
                           :content %
                           :timestamp (:timestamp note)}))
     )


(defrecord DailyLogApp [daily-log ; with event aggregated
                        ideas]
  #_ DailyLogEvents
  #_ (entry-created [model event]
    (accept
      (propose ideas (->CreateIdeaFromEntryCreated event))))


  #_ TodoEvents
  #_ (todo-created [model event])


  agg/Aggregate
  (accept [model event]
    (comment "user dosync for transactional stores. join events? compose results inline at least."))


  #_ Subscriber
  #_ (subscribe [_ to-model]
    {})

  )


(def stores
  {:notes-store (ref {})
   :ideas-store (atom {})})

(def services
  {:notes {}
   :ideas {}
   :hnotes {}})

(def clients
  {:web {}})

(def system (merge stores services clients))

(def deps
  {:notes {:store :notes-store}
   :ideas {:store :ideas-store}
   :hnotes [:ideas :notes]
   :web [:hnotes]})

(defn daily-log-service []
  (let [dl ()]
    (map->DailyLogApp {})))

