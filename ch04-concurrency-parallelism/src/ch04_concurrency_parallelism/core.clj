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
