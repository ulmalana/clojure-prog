(ns ch04-concurrency-parallelism.core
  (:require [clojure.java.io :as io]
           [net.cgrand.enlive-html :as enlive])
  (:use [clojure.string :only (lower-case)])
  (:import [java.net URL MalformedURLException]
           [java.util.concurrent BlockingQueue LinkedBlockingQueue])
  (:gen-class))

;(require '[net.cgrand.enlive-html :as enlive])
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
;(time (dorun (map phone-number files)))

;; parallelize getting phone number from files
;(time (dorun (pmap phone-number files)))

;; chunking (partitioning) dataset for parallelization
;(time (->> files
;           (partition-all 250)
;           (pmap (fn [chunk] (doall (map phone-number chunk))))
;           (apply concat)
;           dorun))

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


;; ref
(defn character
  [name & {:as opts}]
  (ref (merge {:name name :items #{} :health 500}
              opts)))

;; create characters
(def smaug (character "Smaug" :health 500 :strength 400 :items (set (range 50))))
(def bilbo (character "Bilbo" :health 100 :strength 100))
(def gandalf (character "Gandalf" :health 75 :mana 750))

(defn loot
  [from to]
  (dosync
   (when-let [item (first (:items @from))]
     (alter to update-in [:items] conj item)
     (alter from update-in [:items] disj item))))

(defn flawed-loot
  [from to]
  (dosync
   (when-let [item (first (:items @from))]
     (commute to update-in [:items] conj item)
     (commute from update-in [:items] disj item))))

(defn fixed-loot
  [from to]
  (dosync
   (when-let [item (first (:items @from))]
     (commute to update-in [:items] conj item)
     (alter from update-in [:items] disj item))))

(defn attack
  [aggressor target]
  (dosync
   (let [damage (* (rand 0.1) (:strength @aggressor))]
     (commute target update-in [:health] #(max 0 (- % damage))))))

(defn heal
  [healer target]
  (dosync
   (let [aid (* (rand 0.1) (:mana @healer))]
     (when (pos? aid)
       (commute healer update-in [:mana] - (max 5 (/ aid 5)))
       (commute target update-in [:health] + aid)))))

(def alive? (comp pos? :health))

(defn play
  [character action other]
  (while (and (alive? @character)
              (alive? @other)
              (action character other))
    (Thread/sleep (rand-int 50))))

(defn- enforce-max-health
  [name health]
  (fn [character-data]
    (or (<= (:health character-data) health)
        (throw (IllegalStateException. (str name " is already at max health!"))))))

(defn character'
  [name & {:as opts}]
  (let [cdata (merge {:name name :items #{} :health 500}
                     opts)
        cdata (assoc cdata :max-health (:health cdata))
        validators (list* (enforce-max-health name (:health cdata))
                          (:validators cdata))]
    (ref (dissoc cdata :validators)
         :validator #(every? (fn [v] (v %)) validators))))

(def bilbo2 (character' "Bilbo" :health 100 :strength 100))

(defn heal'
  "Support partial healing"
  [healer target]
  (dosync
   (let [aid (min (* (rand 0.1) (:mana @healer))
                  (- (:max-health @target) (:health @target)))]
     (when (pos? aid)
       (commute healer update-in [:mana] - (max 5 (/ aid 5)))
       (alter target update-in [:health] + aid)))))

;; var

;; private var with docstring
(def ^{:private true :doc "this is a private var"}
  priv-var 45)

;; dynamic var
(def ^:dynamic *max-value* 255)

(defn http-get
  [url-string]
  (let [conn (-> url-string java.net.URL. .openConnection)
        response-code (.getResponseCode conn)]
    (if (== 404 response-code)
      [response-code]
      [response-code (-> conn .getInputStream slurp)])))

(def ^:dynamic *response-code* nil)

(defn http-get'
  [url-string]
  (let [conn (-> url-string java.net.URL. .openConnection)
        response-code (.getResponseCode conn)]
    (when (thread-bound? #'*response-code*)
      (set! *response-code* response-code))
    (when (not= 404 response-code) (-> conn .getInputStream slurp))))

;; forward declaration with declare instead of def
(declare val-1 val-2 helper-fn)

;; agent

(def a (agent 500))
(def a1 (agent 0))
(def a2 (agent nil))
(def a3 (agent nil :error-mode :continue))
(def a4 (agent nil
               :error-mode :continue
               :error-handler (fn [the-agent exception]
                                (.println System/out (.getMessage exception)))))

(comment
  (send a range 1000)
  (send a1 inc))

(def console (agent *out*))
(def character-log (agent (io/writer "character-states.log" :append true)))

(defn write
  [^java.io.Writer w & content]
  (doseq [x (interpose " " content)]
    (.write w (str x)))
  (doto w
    (.write "\n")
    .flush))

(defn log-reference
  [reference & writer-agents]
  (add-watch reference :log
             (fn [_ reference old new]
               (doseq [writer-agent writer-agents]
                 (send-off writer-agent write new)))))

(log-reference bilbo console character-log)
(log-reference smaug console character-log)

(defn- links-from
  [base-url html]
  (remove nil? (for [link (enlive/select html [:a])]
                 (when-let [href (-> link :attrs :href)]
                   (try
                     (URL. base-url href)
                     ;; ignore bad urls
                     (catch MalformedURLException e))))))

(defn- words-from
  [html]
  (let [chunks (-> html
                   (enlive/at [:script] nil)
                   (enlive/select [:body enlive/text-node]))]
    (->> chunks
         (mapcat (partial re-seq #"\w+"))
         (remove (partial re-matches #"\d+"))
         (map lower-case))))

(def url-queue (LinkedBlockingQueue.))
(def crawled-urls (atom #{}))
(def word-freqs (atom {}))

(declare get-url)
(def agents (set (repeatedly 25 #(agent {::t #'get-url :queue url-queue}))))

(declare run process handle-results)

(defn ^::blocking get-url
  [{:keys [^BlockingQueue queue] :as state}]
  (let [url (io/as-url (.take queue))]
    (try
      (if (@crawled-urls url)
        state
        {:url url
         :content (slurp url)
         ::t #'process})
      (catch Exception e
        ;; skip any url we failed to load
        state)
      (finally (run *agent*)))))

(defn process
  [{:keys [url content]}]
  (try
    (let [html (enlive/html-resource (java.io.StringReader. content))]
      {::t #'handle-results
       :url url
       :links (links-from url html)
       :words (reduce (fn [m word]
                        (update-in m [word] (fnil inc 0)))
                      {}
                      (words-from html))})
    (finally (run *agent*))))

(defn ^::blocking handle-results
  [{:keys [url links words]}]
  (try
    (swap! crawled-urls conj url)
    (doseq [url links]
      (.put url-queue url))
    (swap! word-freqs (partial merge-with +) words)
    {::t #'get-url :queue url-queue}
    (finally (run *agent*))))

(defn paused?
  [agent]
  (::paused (meta agent)))

(defn run
  ([] (doseq [a agents] (run a)))
  ([a]
   (when (agents a)
     (send a (fn [{transition ::t :as state}]
               (when-not (paused? *agent*)
                 (let [dispatch-fn (if (-> transition meta ::blocking)
                                     send-off
                                     send)]
                   (dispatch-fn *agent* transition)))
               state)))))

(defn pause
  ([] (doseq [a agents] (pause a)))
  ([a] (alter-meta! a assoc ::paused true)))

(defn restart
  ([] (doseq [a agents] (restart a)))
  ([a]
   (alter-meta! a dissoc ::paused)
   (run a)))

(defn test-crawler
  [agent-count starting-url]
  (def agents (set (repeatedly agent-count
                               #(agent {::t #'get-url :queue url-queue}))))
  (.clear url-queue)
  (swap! crawled-urls empty)
  (swap! word-freqs empty)
  (.add url-queue starting-url)
  (run)
  (Thread/sleep 60000)
  (pause)
  [(count @crawled-urls) (count url-queue)])
