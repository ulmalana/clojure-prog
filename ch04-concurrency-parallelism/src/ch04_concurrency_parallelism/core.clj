(ns ch04-concurrency-parallelism.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))


;; delay
(def d (delay (println "Running...")
              :done!))

(defn get-document
  [id]
  {:url "http://www.mozilla.org/about/manifesto.en.html"
   :title "The Mozilla Manifesto"
   :mime "text/html"
   :content (delay (slurp "http://www.mozilla.org/about/manifesto.en.html"))})


;; futures
(def long-calculation (future (apply + (range 1e8))))

(deref (future (Thread/sleep 5000) :done!
               1000
               :impatient!))

(defn get-document-future
  "compared to the delay version, this code will run in a new thread to get :content."
  [id]
  {:url "http://www.mozilla.org/about/manifesto.en.html"
   :title "The Mozilla Manifesto"
   :mime "text/html"
   :content (future (slurp "http://www.mozilla.org/about/manifesto.en.html"))})

;; promise
(def p (promise))

(def a (promise))
(def b (promise))
(def c (promise))

(future
  (deliver c (+ @a @b))
  (println "Delivery complete!"))

(defn call-service
  [arg1 arg2 callback-fn]
  (future (callback-fn (+ arg1 arg2) (- arg1 arg2))))


(defn sync-fn
  [async-fn]
  (fn [& args]
    (let [result (promise)]
      (apply async-fn (conj (vec args) #(deliver result %&)))
      @result)))

;; parallelism
(defn phone-number
  [string]
  (re-seq #"(\d{3})[\.-]?(\d{3})[\.-]?(\d{4})" string))

;; simulate a dummy file
(def files (repeat 100
                   (apply str
                          (concat (repeat 1000000 \space)
                                  "Sunil: 617.444.2945, Berry: 234.948.4423"))))

;; get phone number from files
(time (dorun (map phone-number files)))

;; parallelize getting phone number from files
(time (dorun (pmap phone-number files)))

;; chunking (partitioning) dataset for parallelization
(time (->> files
           (partition-all 250)
           (pmap (fn [chunk] (doall (map phone-number chunk))))
           (apply concat)
           dorun))

;; some helper macros
(defmacro futures
  [n & exprs]
  (vec (for [_ (range n)
             expr exprs]
         `(future ~expr))))

(defmacro wait-futures
  [& args]
  `(doseq [f# (futures ~@args)]
     @f#))

;; atom
(def riz (atom {:name "Riz" :age 25 :wears-glasses? true}))

(def xs (atom #{1 2 3}))

;; watch

(defn echo-watch
  [key identity old new]
  (println key old "=>" new))

(def history (atom ()))

(defn log->list
  [dest-atom key source old new]
  (when (not= old new)
    (swap! dest-atom conj new)))

;; validator
;; check if the value in this atom positif.
;; if negative, then throw an exception.
(def n (atom 1 :validator pos?))
