(ns event-sourcing-clj.domain.todo_test
  (:require [clojure.test :refer :all]
            [event-sourcing-clj.domain.todo :as todo]
            [event-sourcing-clj.infra.aggregate :as agg]))

; no need to do before-each since this is immutable :)
(def todos (todo/make-todos))

(deftest todos-create
  (testing "We can create a new todo"
    (let [todo (todo/map->Todo {:id 1
                                :text "Do it!"
                                :completed? false})
          event (todo/create-new todos 1 "Do it!")]
      (is (= event [:crud/created todo]))))

  (testing "We can't create a duplicate todo (by id)"
    (let [todo (todo/map->Todo {:id 1
                                :text "Do it!"
                                :completed? false})
          event1 (todo/create-new todos 1 todo)
          todos (agg/accept todos event1)
          event2 (todo/create-new todos 1 todo)]
      (is (= event2 nil)))))

(deftest todos-update
  (testing "We can modify a todo (by id) multiple times"
    (let [changed-todo (todo/map->Todo {:id 1
                                        :completed? true})
          ; use domain methods >> setting up state manually
          ; mutatation via shadowing
          create-event (todo/create-new todos 1 "Do it!")
          todos (agg/accept todos create-event)

          event (todo/set-completed todos 1 true)
          expected [:crud/modified changed-todo] ; emit whole or partial record (w id merged or separate)? downstream should have history to materialize old values...
          ]  ; breathing room
      (is (= event expected)) ; could use [_ (is (= ...))] in let, but... let the code smell if test is too long
      (let [todos (agg/accept todos event)
            event (todo/set-completed todos 1 true)]
        (is (= event expected)) ; same expected, we don't check for dedupes or versions
        )))

  (testing "We can't modify a todo that doesn't exist"
    (let [event (todo/set-completed todos 1 true)
          expected nil]
      (is (= event expected)))))

(deftest todos-delete
  (testing "We can delete a todo"
    (let [create-event (todo/create-new todos 1 "Do it!")
          todos (agg/accept todos create-event)

          event (todo/delete todos 1)
          expected [:crud/deleted 1]]
      (is (= event expected))))

  (testing "We can't delete a todo that doesn't exist"
    (let [event (todo/delete todos 1)
          expected nil]
      (is (= event expected))))

  (testing "We can delete completed todos"
    (let [not-done (todo/map->Todo {:id 2 :text "another thing" :completed? false})
          ; explicitly creating state here instead of using domain methods as usually preferred...
          todo (todo/map->TodosAggregate {:todos {1 (todo/map->Todo {:id 1 :text "thing" :completed? true})
                                                   2 not-done
                                                   3 (todo/map->Todo {:id 3 :text "last one" :completed? true})}})


          event (todo/clear-done todos)
          expected [:crud/many-deleted #{1 3}]]
      (is (= event expected))
      ; two step comparison. note: we may want another query method for done.
      (let [todos (agg/accept todos event)]
        (is (= #{not-done} (todo/all-todos todos)))))))

(deftest todos-read
  (testing "We find read a todo we created"
    (let [create-event (todo/create-new todos 1 "Do it!")
          todos (agg/accept todos create-event)

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
    (let [create-event1 (todo/create-new todos 1 "Do it!")
          todos (agg/accept todos create-event1)

          create-event2 (todo/create-new todos 2 "Do more of it!")
          todos (agg/accept todos create-event2)

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
