(ns ch03-collections-data-structure.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn swap-pairs
  [sequential]
  ;; empty will create an empty (non-concrete) collection,
  ;; be it list or vector, or anything else.
  ;; in this case, it depends on the type of the sequential argument.
  (into (empty sequential)
        (interleave
         (take-nth 2 (drop 1 sequential))
         (take-nth 2 sequential))))

(defn random-ints
  [limit]
  (lazy-seq
   (cons (rand-int limit)
         (random-ints limit))))

(defn random-ints'
  "Show how this function realize the lazy seq"
  [limit]
  (lazy-seq
   (println "realizing random number")
   (cons (rand-int limit)
         (random-ints limit))))

(def rands (take 10 (random-ints 50)))

;; associative
(def m {:a 1, :b 2, :c 3})

;; sorted
(def sm (sorted-map :z 5 :x 9 :y 0 :b 2 :a 3 :c 4))
