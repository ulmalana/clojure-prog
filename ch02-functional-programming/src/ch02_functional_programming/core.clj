(ns ch02-functional-programming.core
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.xml :as xml])
  (:gen-class))

(defn call-twice
  [f x]
  (f x)
  (f x))

(def only-strings (partial filter string?))

((partial map *) [1 2 3] [4 5 6] [7 8 9])
; (28 80 162)

;; negation of a sum
(defn negated-sum-str
  [& nums]
  (str (- (apply + nums))))

;; negation of a sum using composition
(def negated-sum-str' (comp str - +))


;; convert CamelCase to lisp keywords
(def camel->keyword
  (comp keyword
        str/join
        (partial interpose \-)
        (partial map str/lower-case)
        #(str/split % #"(?<=[a-z])(?=[A-Z])")))

;; convert CamelCase to lisp keywords using threading macros
(defn camel->keyword'
  [s]
  (->> (str/split s #"(?<=[a-z])(?=[A-Z])")
       (map str/lower-case)
       (interpose \-)
       str/join
       keyword))

;; generate clojure map from a list of key-value pairs that use camelcase keys
(def camel-pairs->map
  (comp (partial apply hash-map)
        (partial map-indexed
                 (fn [i x]
                   (if (odd? i)
                     x
                     (camel->keyword x))))))

;; HOF that returns a function
(defn adder
  [n]
  (fn [x] (+ n x)))

((adder 10) 21)

(defn doubler
  [f]
  (fn [& args]
    (* 2 (apply f args))))

(def double-+ (doubler +))
(double-+ 1 2 3)
;; 12

;; logger
(defn print-logger
  [writer]
  #(binding [*out* writer]
     (println %)))

(def *out*-logger (print-logger *out*))

(def writer (java.io.StringWriter.))
(def retained-logger (print-logger writer))

;; write to character buffer
(retained-logger "this is in char buffer")

;; check the buffer
(str writer)


(defn file-logger
  [file]
  #(with-open [f (io/writer file :append true)]
     ((print-logger f) %)))

;; create a logger to file messages.log
(def log->file (file-logger "messages.log"))

;; write a log message to messages.log
(log->file "halo log")

;; this is the "log router" that forwards messages to multiple loggers
(defn multi-logger
  [& logger-fns]
  #(doseq [f logger-fns]
     (f %)))

;; this will put the message to the loggers
(def log (multi-logger
          (print-logger *out*)
          (file-logger "messages.log")))

(log "hallo again")


(defn timestamped-logger
  [logger]
  #(logger (format "[%1$tY-%1$tm-%1$te %1$tH:%1$tM:%1$tS] %2$s" (java.util.Date.) %)))

(def log-timestamped
  (timestamped-logger
   (multi-logger
    (print-logger *out*)
    (file-logger "messages.log"))))

(log-timestamped "bye now")

(defn twitter-followers
  [username]
  (->> (str "https://api.twitter.com/1/users/show.xml?screen_name=" username)
       xml/parse
       :content
       (filter (comp #{:followers_count} :tag))
       first
       :content
       first
       Integer/parseInt))

(defn prime?
  [n]
  (cond
    (== 1 n) false
    (== 2 n) true
    (even? n) false
    :else (->> (range 3 (inc (Math/sqrt n)) 2)
               (filter #(zero? (rem n %)))
               empty?)))

(time (prime? 1125899906842679))

;; use memoization to cache the result of calling function
(let [m-prime? (memoize prime?)]
  (time (m-prime? 1125899906842679))
  (time (m-prime? 1125899906842679)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
