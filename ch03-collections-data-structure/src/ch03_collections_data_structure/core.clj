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


(defn magnitude
  [x]
  (-> x Math/log10 Math/floor))

(defn compare-magnitude
  [a b]
  (- (magnitude a) (magnitude b)))

(defn compare-magnitude'
  [a b]
  (let [diff (- (magnitude a) (magnitude b))]
    (if (zero? diff)
      (compare a b)
      diff)))

(defn interpolate
  "Takes a collection of points (as [x y] tuples), return a function which is a linear interpolation between those points"
  [points]
  (let [results (into (sorted-map) (map vec points))]
    (fn [x]
      (let [[xa ya] (first (rsubseq results <= x))
            [xb yb] (first (subseq results > x))]
        (if (and xa xb)
          (/ (+ (* ya (- xb x)) (* yb (- x xa)))
             (- xb xa))
          (or ya yb))))))

(def f (interpolate [[0 0] [10 10] [15 5]]))
(map f [2 10 12])


;; vector as tuples
(defn euclidian-division
  [x y]
  [(quot x y) (rem x y)])

;; set
(defn numeric?
  [s]
  (every? (set "0123456789") s))

;; maps
(defn reduced-by
  [key-fn f init coll]
  (reduce (fn [summaries x]
            (let [k (key-fn x)]
              (assoc summaries k (f (summaries k init) x))))
          {}
          coll))

(def orders
  [{:product "Clock" :customer "Wile Coyote" :qty 6 :total 300}
   {:product "Dynamite" :customer "Wile Coyote" :qty 20 :total 5000}
   {:product "Shotgun" :customer "Elmer Fudd" :qty 2 :total 800}
   {:product "Shells" :customer "Elmer Fudd" :qty 4 :total 100}
   {:product "Hole" :customer "Wile Coyote" :qty 1 :total 1000}
   {:product "Anvil" :customer "Elmer Fudd" :qty 2 :total 300}
   {:product "Anvil" :customer "Wile Coyote" :qty 6 :total 900}])


;; transient: reference to old values may change as well
(def x (transient []))
(def y (conj! x 1))

(count y) ; => 1
(count x) ; => 1, because x follows y.

;; naive into
(defn naive-into
  [coll source]
  (reduce conj coll source))

;; faster than naive into
(defn faster-into
  [coll source]
  (persistent! (reduce conj! (transient coll) source)))
