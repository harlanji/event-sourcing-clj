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
          event (todo/create-new todos 1 todo)]
      (is (= event [::todo/todo-created todo]))))

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
    (let [todo (todo/map->Todo {:id 1
                                :text "Do it!"
                                :completed? false})
          ; use domain methods >> setting up state manually
          ; mutatation via shadowing
          create-event (todo/create-new todos 1 todo)
          todos (agg/accept todos create-event)

          event (todo/modify todos 1 {:completed? true})
          expected [::todo/todo-modified {:id 1 :completed? true}] ; emit whole or partial record (w id merged or separate)? downstream should have history to materialize old values...
          ]  ; breathing room
      (is (= event expected)) ; could use [_ (is (= ...))] in let, but... let the code smell if test is too long
      (let [todos (agg/accept todos event)
            event (todo/modify todos 1 {:completed? true})]
        (is (= event expected)) ; same expected, we don't check for dedupes
        )))

  (testing "We can't modify a todo that doesn't exist"
    (let [event (todo/modify todos 1 {:completed? true})
          expected nil]
      (is (= event expected)))))

(deftest todos-delete
  (testing "We can delete a todo"
    (let [todo (todo/map->Todo {:id 1
                                :text "Do it!"
                                :completed? false})
          create-event (todo/create-new todos 1 todo)
          todos (agg/accept todos create-event)

          event (todo/delete todos 1)
          expected [::todo/todo-deleted 1]]
      (is (= event expected))))

  (testing "We can't delete a todo that doesn't exist"
    (let [event (todo/delete todos 1)
          expected nil]
      (is (= event expected)))))

(deftest todos-read
  (testing "We find read a todo we created"
    (let [todo (todo/map->Todo {:text "Do it!"
                                :completed? false})
          create-event (todo/create-new todos 1 todo)
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
    (let [todo1 (todo/map->Todo {:text "Do it!"
                                :completed? false})
          create-event1 (todo/create-new todos 1 todo1)
          todos (agg/accept todos create-event1)

          todo2 (todo/map->Todo {:text "Do more of it!"
                                 :completed? false})
          create-event2 (todo/create-new todos 2 todo2)
          todos (agg/accept todos create-event2)

          found (todo/all-todos todos)
          expected #{(todo/map->Todo {:id 1
                                    :text "Do it!"
                                    :completed? false})
                     (todo/map->Todo {:id 2
                                      :text "Do more of it!"
                                      :completed? false})}]
      (is (= found expected))))

  )

; map is subset of... map diff is nil? (clojure.data/diff
