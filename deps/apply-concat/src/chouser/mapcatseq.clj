(ns chouser.mapcatseq
  (:require [clojure.string :as str]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.pprint :refer [print-table]]
            [clojure.repl :refer :all]))

(defn find-phrases []
  (let [xs [[1 2 3] [4 5 6] [7 8 9]]
        goal (mapcat seq xs)
        exclude-syms '#{iterate gen-interface split-at primitives-classnames
                        var-set trampoline cycle hash-ordered-coll var? meta
                        read+string volatile? seque refer-clojure bytes
                        denominator fnext spit}
        vars (->> (vals (ns-publics 'clojure.core))
              (remove #(exclude-syms (.sym %)))
              (filter #(instance? clojure.lang.IFn @%)))]

    (doall
     (for [v0 vars
           v1 vars
           :when (not (= '#{[keep-indexed take-nth] [bytes bytes]}
                         [(.sym v0) (.sym v1)]))
           :when (try
                    (= goal (@v0 @v1 xs))
                    (catch Throwable t false))]
       (list v0 v1 'xs)))))

(defn lazier-mapcat [f coll]
  (lazy-seq
   (when-let [[xs] (seq coll)]
     (if-let [[x] (seq xs)]
       (cons x (lazier-mapcat f (cons (rest xs) (rest coll))))
       (lazier-mapcat f (rest coll))))))

;; eduction cat
;; reduce concat
;; mapcat macroexpand
;; mapcat eduction

(def phrases
  '[(flatten)
    (apply concat)
    (mapcat identity)
    (mapcat seq)
    (reduce into)
    (sequence cat)
    (eduction cat)
    #_(lazier-mapcat identity) ;; not worth talking about
    #_(apply into)]) ;; doesn't even work

(def squashers
  (for [syms phrases]
    [(str/join " " syms)
     (eval `(fn [xs#] (~@syms xs#)))]))

(defn t0 [a b]
  (prop/for-all [v (gen/vector (gen/vector gen/int))]
    (= (a v) (b v))))

(defn catch-all [f]
  (fn [xs]
    (try
      (doall (f xs)) ;; laziness!
      (catch Throwable t
        :error))))

(defn test-all [num t]
  (doseq [[k f] squashers]
    (prn k (tc/quick-check num (t (catch-all (:mcs squashers)) (catch-all f))))))

(defn t1 [a b]
  (prop/for-all [v (gen/vector (gen/vector (gen/vector gen/int)))]
                (= (a v) (b v))))

(defn t2 [a b]
  (prop/for-all [v gen/any]
                (= (a v) (b v))))

(def ^:dynamic *hold-the-lazy* false)

(defn write-lazy-seq [space? coll w]
  (if (and *hold-the-lazy*
           (instance? clojure.lang.IPending coll)
           (not (realized? coll)))
    (.write w (str (when space? " ") "..."))
    (when (seq coll)
      (when space?
        (.write w " "))
      (.write w (pr-str (first coll)))
      (write-lazy-seq true (rest coll) w))))

(defmethod print-method clojure.lang.ISeq [coll w]
  (.write w "(")
  (write-lazy-seq false coll w)
  (.write w ")"))

(defn seq1 [s]
  (lazy-seq
   (when-let [[x] (seq s)]
     (cons x (seq1 (rest s))))))

(defn observe-laziness [apply-seq]
  (cons
    {:phrase "<i>forced</i>"
     :input (binding [*hold-the-lazy* false]
              (pr-str (apply-seq identity)))}
    (for [[phrase f] squashers]
      (let [input (apply-seq f)]
        (binding [*hold-the-lazy* true]
          {:phrase phrase :input (pr-str input)})))))

(defn print-laziness [results]
  (print-table [:phrase :input] results))

(defn html-laziness [results]
  (println "<table>")
  (doseq [{:keys [phrase input]} results]
    (println (format "<tr><td>%s</td> <td>%s</td></tr>"
                     phrase input)))
  (println "</table>"))

(defn d2-short [f]
  (doto
    (seq1 [(seq1 [1 2 3]) (seq1 [4 5 6]) (seq1 [7 8 9]) (seq1 [10 11 12])])
    f))

(defn d2-short-force1 [f]
  (doto
    (seq1 [(seq1 [1 2 3]) (seq1 [4 5 6]) (seq1 [7 8 9]) (seq1 [10 11 12])])
    (-> f first)))

(defn d2-long [f]
  (doto
    (seq1 (map (fn [i] (seq1 (range (* i 3) (+ (* i 3) 3)) (range 5)))))
    f))

(defn d3 [f]
  (doto
    (seq1 [(seq1 [1 2 (seq1 [3 4])]) (seq1 [5 6 (seq1 [7 8])])])
    f))

;; read+string hash-ordered-coll

(comment
  (def ps (find-phrases))

  (test-all 500 t0)
  (test-all 100 t1)
  (test-all 10 t2)

  (print-laziness (observe-laziness d3))
  (html-laziness (observe-laziness d3)))

;; chouser.mapcatseq=>   (print-laziness (observe-laziness d2-short))
;; |         :phrase |                                 :input |
;; |-----------------+----------------------------------------|
;; |   <i>forced</i> |   ((1 2 3) (4 5 6) (7 8 9) (10 11 12)) |
;; |         flatten |          ((...) (...) (...) (...) ...) |
;; |     reduce into |   ((1 ...) (4 5 6) (7 8 9) (10 11 12)) |
;; |    apply concat |          ((...) (...) (...) (...) ...) |
;; | mapcat identity |          ((...) (...) (...) (...) ...) |
;; |      mapcat seq | ((1 ...) (4 ...) (7 ...) (10 ...) ...) |
;; |    sequence cat |                          ((1 2 3) ...) |
;; |    eduction cat |                                  (...) |
