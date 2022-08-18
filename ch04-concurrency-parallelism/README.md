# Chapter 04 - Concurrency Parallelise

## Shifting computation through time and space

We can control how and when computations are perfomed using **delays**, **futures**, and **promises**.

### Delays

We can **suspend** some body of code *until* evaluating it **on demand** when it is dereferenced. We can check whether the values have been realized with `realized?`.

Delays' advantages:
* evaluate the body of code **only once**.
* possible to get expensive-to-produce values because execute once.
* multiple threads can safely attemp to deref the value.

### Futures

With `future`, we can evaluate a body of code in another thread. `future` returns **immediately**, allowing the current thread of execution to carry on. We can also set a **timeout** for `future`.

Futures' advantages:
* more concise than setting up and starting native threads.
* evaluated within a thread pool, which is more efficient than creating new threads.
* easier to interoperate with Java.

### Promises

With `promise`, we can create something with defining it (eg. like creating **an empty box/container**). At some later point, we **can deliver** something to `promise` to fulfill it.

## Clojure Reference Types

There are four reference types which are similar to boxes that hold a value, where **that value can be changed**: `var`, `ref`, `agent`, and `atom`. Derefing will return a *snapshot* of the state and it will not block (unlike `delay`, `promise`, and `future`).

All reference types:
* maybe decorated with metadata.
* can notify functions when their state changes (*watches*).
* can enforce constraints on the state (*validator*).

## Classifying concurrent operations

### Coordinated
A coordinated operation is one where **multiple actors must cooperate to yield correct results**. Example: bacnk transaction.

An **uncoordinated** operation is one where **multiple actors cannot impact each other negatively** because their contexts are **separated**.

### Synchronization

**Synchronous** operations are those where the caller's thread **waits or blocks or sleeps** until it has exclusive access.

Asynchronous operations are those that can be **started or scheduled without blocking**.

|     | **Coordinated** | **Uncoordinated** |
| --- | --- | --- |
| **Synchronous** | `ref` | `atom` |
| **Asynchronous** | - | `agent` |

## Atoms

* Most basic reference type
* **Synchronous and uncoordinated**.
* Modifying an atom **will block** until modification is complete.
* Each modification is **isolated** (no way to orchestrate modifying two atoms).
* `swap!` is used to modify atom with some function.
* Because of *compare-and-set* semantics, `swap!` will retry when the old value is different.
* We can `reset!` an atom to certain value.

## Watches

We can use watches to **observe any change** in reference types. It is just a function and we can attach/detach it on reference types with `add-watch` and `remove-watch`.

## Validators

Validators can be used to constrain a reference's state. It will check if the **proposed value** is still accepted. If it is not, then there will be no change. We can insert one-argument function with `:validator` key when creating a reference.

## Refs

* **Coordinated** reference type. Can be used to manipulate multiple identites.
* Refs are useful for handling **Software Transactional Memory (STM)** Every transaction is first established with `dosync` and must be performed inside it.
* There are two functions to change the state: `alter` and `commute`.
* `alter` guarantees **in-order** transcation with the price of retrying.
* `commute` doesnt have retries but with the price of **inconsistent data**.

### Sharp corners of STM
* Side-effections functions are strictly **forbidden**, only run functions that are safe to retry in the STM scope (ex: writing to database. it would be inefficient to retry this function.)
* **Minimize the scope** of each transaction. Big transactions or computationally expensive ones may affect the performance.
  * Old transaction can be forced to proceed with **barging**


## Vars

Vars can be used to **associate symbols** (functions, values, etc) **and its corresponding objects** in the namespace. We can create a var (or association) with `def`. We can create a private var with specifying `:private` metadata.
```clj
(def ^:private everything 45)

; or

(def ^{:private true} everything 45)
```

We can also set a var to be a **constant** with `^:const`, so that any modification in the future is prohibited.

### Dynamic Scope

We can create a var that has dynamic scope (ie. can be accessed from anywhere. similar to global variables) with `^:dynamic`.

## Agents

* **Uncoordinated** and **asynchronous** reference type. IO and other side-effecting functions can be safely used with agent. Agent are also STM-aware. We can change agent's state with `send` and `send-off`.

Agent *action* = `(send/send-off fns args)`
