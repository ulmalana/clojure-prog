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

;; conway's game of life
(defn empty-board
  "Creates a rectangular empty board of the specified with and height"
  [w h]
  (vec (repeat w (vec (repeat h nil)))))

(defn populate
  "Turns :on wach of the cells specified as [y, x] coordinates"
  [board living-cells]
  (reduce (fn [board coordinates]
            (assoc-in board coordinates :on))
          board
          living-cells))

;; print this board with (pprint glider)
(def glider (populate (empty-board 6 6) #{[2 0] [2 1] [2 2]
                                          [1 2] [0 1]}))

(defn neighbours
  [[x y]]
  (for [dx [-1 0 1] dy [-1 0 1] :when (not= 0 dx dy)]
    [(+ dx x) (+ dy y)]))

(defn count-neighbours
  [board loc]
  (count (filter #(get-in board %) (neighbours loc))))

(defn indexed-step
  "Yields the next state of the board, using indices to determine neighbours, liveness, etc."
  [board]
  (let [w (count board)
        h (count (first board))]
    (loop [new-board board x 0 y 0]
      (cond
        (>= x w) new-board
        (>= y h) (recur new-board (inc x) 0)
        :else (let [new-liveness
                    (case (count-neighbours board [x y])
                      2 (get-in board [x y])
                      3 :on
                      nil)]
                (recur (assoc-in new-board [x y] new-liveness) x (inc y)))))))

(defn indexed-step2
  [board]
  (let [w (count board)
        h (count (first board))]
    (reduce
     (fn [new-board x]
       (reduce
        (fn [new-board y]
          (let [new-liveness
                (case (count-neighbours board [x y])
                  2 (get-in board [x y])
                  3 :on
                  nil)]
            (assoc-in new-board [x y] new-liveness)))
        new-board (range h)))
     board (range w))))

(defn indexed-step3
  [board]
  (let [w (count board)
        h (count (first board))]
    (reduce
     (fn [new-board [x y]]
       (let [new-liveness
             (case (count-neighbours board [x y])
               2 (get-in board [x y])
               3 :on
               nil)]
         (assoc-in new-board [x y] new-liveness)))
     board (for [x (range h) y (range w)] [x y]))))


(defn window
  "Returns a lazy sequence of 3-item windows centered around item of coll."
  [coll]
  (partition 3 1 (concat [nil] coll [nil])))

(defn cell-block
  "Creates a sequence of 3x3 windows from a triple of 3 seqs."
  [[left mid right]]
  (window (map vector
               (or left (repeat nil)) mid (or right (repeat nil)))))

(defn window2
  "Returns a lazy seq of 3-item windows centered around each item of coll,
  padded as necessary with pad or nil."
  ([coll] (window2 nil coll))
  ([pad coll]
   (partition 3 1 (concat [pad] coll [pad]))))

(defn cell-block2
  "Creates a seq of 3x3 windows from a triple of 3 seqs"
  [[left mid right]]
  (window2 (map vector left mid right)))

(defn liveness
  "Returns the liveness (nil or :on) of the centerl cell for the next step"
  [block]
  (let [[_ [_ center _] _] block]
    (case (- (count (filter #{:on} (apply concat block)))
             (if (= :on center) 1 0))
      2 center
      3 :on
      nil)))

(defn- step-row
  "Yields the next state of the center row."
  [rows-triple]
  (vec (map liveness (cell-block2 rows-triple))))

;; index-free-step is equivalent to indexed-step
(defn index-free-step
  "Yields the next state of the board."
  [board]
  (vec (map step-row (window2 (repeat nil) board))))

;; elegant step
(defn step
  "Yields the next state of the world"
  [cells]
  (set (for [[loc n] (frequencies (mapcat neighbours cells))
             :when (or (= n 3) (and (= n 2) (cells loc)))]
         loc)))

(defn stepper
  "Returns a step function for life-like cell automata.
  neighbours take a location and return a sequential collecition of locations.
  survice? and birth? are predictaes on the number of living neighbours"
  [neighbours birth? survive?]
  (fn [cells]
    (set (for [[loc n] (frequencies (mapcat neighbours cells))
               :when (if (cells loc) (survive? n) (birth? n))]
           loc))))

(defn hex-neighbours
  [[x y]]
  (for [dx [-1 0 1] dy (if (zero? dx) [-2 2] [-1 1])]
    [(+ dx x) (+ dy y)]))

(def hex-step (stepper hex-neighbours #{2} #{3 4}))


;; wilson's maze generator
(defn maze
  "Returns a random maze carved out of walls. walls is a set of 2-item
  sets #{a b} where a and b are locations.
  The returned maze is a set of the remaining walls."
  [walls]
  (let [paths (reduce (fn [index [a b]]
                        (merge-with into index {a [b] b [a]}))
                      {}
                      (map seq walls))
        start-loc (rand-nth (keys paths))]
    (loop [walls walls
           unvisited (disj (set (keys paths)) start-loc)]
      (if-let [loc (when-let [s (seq unvisited)] (rand-nth s))]
        (let [walk (iterate (comp rand-nth paths) loc)
              steps (zipmap (take-while unvisited walk) (next walk))]
          (recur (reduce disj walls (map set steps))
                 (reduce disj unvisited (keys steps))))
        walls))))


(defn grid
  [w h]
  (set (concat
        (for [i (range (dec w)) j (range h)] #{[i j] [(inc i) j]})
        (for [i (range w) j (range (dec h))] #{[i j] [i (inc j)]}))))

(defn draw
  [w h maze]
  (doto (javax.swing.JFrame. "Maze")
    (.setContentPane
     (doto (proxy [javax.swing.JPanel] []
             (paintComponent [^java.awt.Graphics g]
               (let [g (doto ^java.awt.Graphics2D (.create g)
                         (.scale 10 10)
                         (.translate 1.5 1.5)
                         (.setStroke (java.awt.BasicStroke. 0.4)))]
                 (.drawRect g -1 -1 w h)
                 (doseq [[[xa ya] [xb yb]] (map sort maze)]
                   (let [[xc yc] (if (= xa xb)
                                   [(dec xa) ya]
                                   [xa (dec ya)])]
                     (.drawLine g xa ya xc yc))))))
       (.setPreferredSize (java.awt.Dimension.
                           (* 10 (inc w)) (* 10 (inc h))))))
    .pack
    (.setVisible true)))

;; original wilson's maze generation algorithm
(defn wmaze
  "The original Wilson's algorithm"
  [walls]
  (let [paths (reduce (fn [index [a b]]
                        (merge-with into index {a [b] b [a]}))
                      {}
                      (map seq walls))
        start-loc (rand-nth (keys paths))]
    (loop [walls walls
           unvisited (disj (set (keys paths)) start-loc)]
      (if-let [loc (when-let [s (seq unvisited)] (rand-nth s))]
        (let [walk (iterate (comp rand-nth paths) loc)
              steps (zipmap (take-while unvisited walk) (next walk))
              walk (take-while identity (iterate steps loc))
              steps (zipmap walk (next walk))]
          (recur (reduce disj walls (map set steps))
                 (reduce disj unvisited (keys steps))))
        walls))))

;; hex maze
(defn hex-grid
  [w h]
  (let [vertices (set (for [y (range h) x (range (if (odd? y) 1 0) (* 2 w) 2)]
                        [x y]))
        deltas [[2 0] [1 1] [-1 1]]]
    (set (for [v vertices d deltas f [+ -]
               :let [w (vertices (map f v d))]
               :when w] #{v w}))
    ))


(defn- hex-outer-walls
  [w h]
  (let [vertices (set (for [y (range h) x (range (if (odd? y) 1 0) (* 2 w) 2)]
                        [x y]))
        deltas [[2 0] [1 1] [-1 1]]]
    (set (for [v vertices d deltas f [+ -]
               :let [w (map f v d)]
               :when (not (vertices w))] #{v (vec w)}))))

(defn hex-draw
  [w h maze]
  (doto (javax.swing.JFrame. "Hex Maze")
    (.setContentPane
     (doto (proxy [javax.swing.JPanel] []
             (paintComponent [^java.awt.Graphics g]
               (let [maze (into maze (hex-outer-walls w h))
                     g (doto ^java.awt.Graphics2D (.create g)
                         (.scale 10 10)
                         (.translate 1.5 1.5)
                         (.setStroke (java.awt.BasicStroke. 0.4
                                                            java.awt.BasicStroke/CAP_ROUND
                                                            java.awt.BasicStroke/JOIN_MITER)))
                     draw-line (fn [[[xa ya] [xb yb]]]
                                 (.draw g
                                        (java.awt.geom.Line2D$Double.
                                         xa (* 2 ya) xb (* 2 yb))))]
                 (doseq [[[xa ya] [xb yb]] (map sort maze)]
                   (draw-line
                    (cond
                      (= ya yb) [[(inc xa) (+ ya 0.4)] [(inc xa) (- ya 0.4)]]
                      (< ya yb) [[(inc xa) (+ ya 0.4)] [xa (+ ya 0.6)]]
                      :else [[(inc xa) (- ya 0.4)] [xa (- ya 0.6)]]))))))
       (.setPreferredSize (java.awt.Dimension.
                           (* 20 (inc w)) (* 20 (+ 0.5 h))))))
    .pack
    (.setVisible true)))
