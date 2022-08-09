# Chapter 03 - Collections and Data Structures

## Abstractions

There are **7** primary abstractions of Clojure data structures:
1. **Collection**
2. **Sequence**
3. **Associative**
4. **Indexed**
5. **Stack**
6. **Set**
7. **Sorted**

### Collection

We can use the following functions to values that support collection abstraction:
* `conj`: **add item** to a collection.
* `seq`: **get a sequence** of a collection.
* `count`: **get the number of items** in a collection.
* `empty`: **get an empty instance of the same type**.
* `=`: determine the equality.

### Sequence

Sequence abstraction defines a way **to obtain and traverse sequential views** over som values. Aside from functions from collection abstraction, it also supports:
* `seq`: produces a sequence over its argument.
* `first`, `rest`, `next`: ways to consume seqs.
* `lazy-seq`: produces a **lazy sequence**. 

### Associative

Associative abstraction is shared by data structures that link keys and values: There are four operations:
* `assoc`: **create a new association** (keys and values) within a collection.
* `dissoc`: **drops given associations** from the collection.
* `get`: **looks up the value** for a particular key.
* `contains?`: check if the collection **has a value** associated with given key.

Map is the canonical associative data structure, but **vector is also associative** (associate values with indices).

### Indexed

Indexed supported function:
* `nth`: get a value from a collection. In out-of-bound cases, `nth` returns exception, `get` returns `nil`.

### Stack

Clojure has no stack data structure, but it supports stack anbstraction with three functions:
* `conj`: push (add) value onto the stack.
* `pop`: obtain the stack with its top removed.
* `peek`: obtain the value top of the stack.

### Set

Set is similar to associative abstraction but also need `disj` to remove values from the set.

### Sorted

Sorted abstraction guarantee that the values in the collection are in order. Supported function:
* `rseq`: returns a seq of collections values in reverse, constant time guaranteed.
* `subseq`: returns a seq of collections values within a specified range of keys.
* `rsubseq`: similar to `subseq`, but in reversed order.

Only **maps** and **sets** are available in sorted variants.

```clj
(def sm (sorted-map :z 5 :x 9 :y 0 :b 2 :a 3 :c 4))

sm
; => {:a 3, :b 2, :c 4, :x 9, :y 0, :z 5}

(rseq sm)
; => ([:z 5] [:y 0] [:x 9] [:c 4] [:b 2] [:a 3])

(subseq sm <= :c)
; => ([:a 3] [:b 2] [:c 4])

(rsubseq sm > :b <= :y)
; => ([:y 0] [:x 9] [:c 4])
```
