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
