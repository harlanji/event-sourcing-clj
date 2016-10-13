# Model Design

A model is like a scene and threads are like actors. We can have several actors on stage interacting with things and each other, but if they were to close their eyes and blindly try to interact with objects they knew about, they might start bumping into other actors who cross their paths or try to interact with the same object.

So today when we talk about a model, we generally assume that there's a single actor or that several actors can coordinate with each other. We all know from meetings that mo' coordination mo' problems. Does the analogy hold when we're talking about software? Afterall, locks and network communication happens FAST, way faster than a person could keep track of.

Plus everyone uses REST anyway... I mean CRUD. This is true, your programmers are used to writing CRUD backed MVC style code (eg. Rails). But that's just code organization. What if you could write code exactly like you're used to but follow a few rules, and get all the benefits of an ES system?

Now you can. Well in theory, in practice it needs some work but it just needs to be done.

```
(defprotocol TodoCommands
  (create-new [_ id text])
  (change-text [_id new-text])
  (change-completed [_ id completed?])
  (delete [_ id])
  (clear-done [_]))
  
(defprotocol TodoMutators
  )
```

generates commands


```clojure

```

and binding to the supermodel

```clojure
  TodoCommands ; interface for editor completion... usefulness can be debated
  (create-new [model id text]
    (agg/valid? (->CreateNew id text) model))
  (change-text [model id new-text]
    (agg/valid? (->ChangeText id new-text) model))
  (change-completed [model id completed?]
    (agg/valid? (->ChangeCompleted id completed?) model))
  (delete [model id]
    (agg/valid? (->Delete id) todos))
  (clear-done [model]
    (agg/valid? (->ClearDone) model))
```


```clojure

```



commands themselves could have mutator signatures, 2 way contract.


-


https://maurits.wordpress.com/2011/01/13/find-which-protocols-are-implemented-by-a-clojure-datatype/

-

```
(defrecord Greeter [memory]
  SayIt
  (just-say-it [model name]
    (let [message (str name ": say hello!")]
      (println message)
      (update model conj memory message))))

; filter out {:update true} meta before return (follow ptr)
; kw could resolve based on current scope. could be a multi-method that allows registration for kw-based dispatch. default if no override o(1) map.
(defrecord Greeter2 [memory]
  SayIt
  (just-say-it [model name]
    (let [message (str name ": say hello!")]
      (println message)
      ^:clojure.core/update (conj memory message))))

```