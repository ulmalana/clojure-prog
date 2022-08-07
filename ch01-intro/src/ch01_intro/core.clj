(ns ch01-intro.core
  (:gen-class))

;; compute average
(defn average
  "Compute the average from a list of numbers"
  [nums]
  (/ (apply + nums) (count nums)))

(def person {:name "Kamado Tanjiro"
             :city "Kyoto"})

;; ::location is namespaced keywords which is expanded
;; to ch01-intro.core/location
(def pizza {:name "Ramunto's"
            :location "Claremont, NH"
            ::location "43.3734,-72.3365"})

;; commas are considered whitespace
(defn silly-adder
  [x y]
  (+, x, y))

;; compute hypothenuse
(defn hypot
  [x y]
  (let [x2 (* x x)
        y2 (* y y)]
    (Math/sqrt (+ x2 y2))))

(def v [42 "foo" 99.2 [5 12]])
(def m {:a 5 :b 6
        :c [7 8 9]
        :d {:e 10 :f 11}
        "foo" 88
        42 false})

(def strange-adder (fn adder-self-ref
                     ([x] (adder-self-ref x 1))
                     ([x y] (+ x y))))

;; variadic function
(defn concat-rest
  [x & rest]
  (apply str (butlast rest)))

(defn make-user
  [& [user-id]]
  {:user-id (or user-id
                (str (java.util.UUID/randomUUID)))})

(defn make-user'
  [username & {:keys [email join-date]
               :or {join-date (java.util.Date.)}}]
  {:username username
   :join-date join-date
   :email email
   :exp-date (java.util.Date. (long (+ 2.592e9 (.getTime join-date))))})

(defn countdown
  [x]
  (if (zero? x)
    :blastoff!
    (do (println x)
        (recur (dec x)))))

;; reimplement clojure's repl
(defn embedded-repl
  "A naive clojure repl implementation. Enter `:quit` to exit"
  []
  (print (str (ns-name *ns*) ">>> "))
  (flush)
  (let [expr (read)
        value (eval expr)]
    (when (not= :quit value)
      (println value)
      (recur))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
