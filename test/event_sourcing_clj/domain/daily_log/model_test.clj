(ns event-sourcing-clj.domain.daily-log.model_test
  (:require [clojure.test :refer :all]
            [event-sourcing-clj.domain.daily-log.core :refer :all :as dl-core]
            ;[event-sourcing-clj.domain.daily-log.core :refer :all :as dl-ideas]
            [event-sourcing-clj.domain.daily-log.model :refer :all :as dl-model]))

(deftest notes
  (testing "Create note"
    (let [dl (-> (map->DailyLog {:notes {}
                                 :idea-id-factory identity})
                 (create-note "1" "today")
                 (append-entry "1" "1.1" "some stuff" "now")
                 (append-entry "1" "1.2" "some more stuff" "now"))
          entries1 (dl-core/entries-for-note dl "1")
          expected1 [(->Entry "1" "1.1" "some stuff" "now")
                     (->Entry "1" "1.2" "some more stuff" "now")]]
      (is (= entries1 expected1)))))


#_ (deftest ideas
  (testing "Extract ideas"
    (let [dl (-> (map->DailyLog {:notes {}
                                 :idea-id-factory (constantly "1")})
                 (create-note "1" "today")
                 (append-entry "1" "1.1" "some stuff" "now")
                 (append-entry "1" "1.2" "idea: a cool idea" "now"))
          entries1 (dl-ideas/ideas-for-note dl "1")
          expected1 [(->Idea "1" "1" "a cool idea" "now")]]
      (is (= entries1 expected1)))))