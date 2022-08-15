# Chapter 04 - Concurrency Parallelism

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
