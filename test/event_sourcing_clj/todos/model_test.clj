(ns event-sourcing-clj.todos.model_test
  (:require [clojure.test :refer :all]
            [event-sourcing-clj.todos.domain.core :as todo]
            [event-sourcing-clj.todos.domain.model :refer [make-todos map->Todos]]

            [event-sourcing-clj.infra.aggregate :as agg]))

; no need to do before-each since this is immutable :)
(def todos (make-todos))

(deftest todos-create
  (testing "We can create a new todo"
    (let [event (agg/propose (todo/->CreateNew 1 "Do it!") todos)]
      (is (= event (todo/->Created 1 "Do it!" false)))))

  (testing "We can't create a duplicate todo (by id)"
    (let [event1 (agg/propose (todo/->CreateNew 1 "Do it!") todos)
          todos (agg/accept event1 todos)
          event2 (agg/propose (todo/->CreateNew 1 "Do it!") todos)]
      (is (= event2 nil)))))

(deftest todos-update
  (testing "We can modify a todo (by id) multiple times"
    (let [; use domain methods >> setting up state manually
          ; mutatation via shadowing
          create-event (agg/propose (todo/->CreateNew 1 "Do it!") todos)
          todos (agg/accept create-event todos)

          event (agg/propose (todo/->ChangeCompleted 1 true) todos)
          expected (todo/->CompletedChanged 1 true) ; emit whole or partial record (w id merged or separate)? downstream should have history to materialize old values...
          ]  ; breathing room
      (is (= event expected)) ; could use [_ (is (= ...))] in let, but... let the code smell if test is too long
      (let [todos (agg/accept event todos)
            event (agg/propose (todo/->ChangeCompleted 1 true) todos)]
        (is (= event expected)) ; same expected, we don't check for dedupes or versions
        )))

  (testing "We can't modify a todo that doesn't exist"
    (let [event (agg/propose (todo/->ChangeCompleted 1 true) todos)
          expected nil]
      (is (= event expected)))))

(deftest todos-delete
  (testing "We can delete a todo"
    (let [create-event (agg/propose (todo/->CreateNew 1 "Do it!") todos)
          todos (agg/accept create-event todos)

          event (agg/propose (todo/->Delete 1) todos)
          expected (todo/->Deleted 1)]
      (is (= event expected))))

  (testing "We can't delete a todo that doesn't exist"
    (let [event (agg/propose (todo/->Delete 1) todos)
          expected nil]
      (is (= event expected))))

  (testing "We can delete completed todos"
    (let [not-done (todo/map->Todo {:id 2 :text "another thing" :completed? false})
          ; explicitly creating state here instead of using domain methods as usually preferred...
          todos (map->Todos {:store {1 (todo/map->Todo {:id 1 :text "thing" :completed? true})
                                          2 not-done
                                          3 (todo/map->Todo {:id 3 :text "last one" :completed? true})}})


          event (agg/propose (todo/->ClearDone) todos)
          expected (todo/->DoneCleared #{1 3})]
      (is (= event expected))
      ; two step comparison. note: we may want another query method for done.
      (let [todos (agg/accept event todos)]
        (is (= #{not-done} (todo/all-todos todos)))))))

(deftest todos-read
  (testing "We find read a todo we created"
    (let [create-event (agg/propose (todo/->CreateNew 1 "Do it!") todos)
          todos (agg/accept create-event todos)

          found(todo/get-todo todos 1)
          expected (todo/map->Todo {:id 1
                                    :text "Do it!"
                                    :completed? false})]
      (is (= found expected))))

  (testing "We can't read a todo that doesn't exist"
    (let [event (todo/get-todo todos 1)
          expected nil]
      (is (= event expected))))


  (testing "We can read all todos that we created"
    (let [create-event1 (agg/propose (todo/->CreateNew 1 "Do it!") todos)
          todos (agg/accept create-event1 todos)

          create-event2 (agg/propose (todo/->CreateNew 2 "Do more of it!") todos)
          todos (agg/accept create-event2 todos)

          found (todo/all-todos todos)
          expected #{(todo/map->Todo {:id 1
                                    :text "Do it!"
                                    :completed? false})
                     (todo/map->Todo {:id 2
                                      :text "Do more of it!"
                                      :completed? false})}]
      (is (= (count found) (count expected)))
      (is (= found expected))))

  )

; map is subset of... map diff is nil? (clojure.data/diff
