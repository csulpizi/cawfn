(ns cawfn.parsing
  "Defines functions to parse arguments given to `cawfn` macro."
  (:require [cawfn.errors :as e]
            [clojure.set :refer [union]]))

(defn parse-macro-args
  "Expand arguments to the cawfn macro
  (of the form [`fn-name` `doc-string?` `attr-map?` `params` `body`])
   into a map."
  [args]
  (let [[fn-name & more] args]
    (when-not (symbol? fn-name) (e/throw!-name-error))
    (loop [m {:fn-name fn-name
              :param-sym (gensym)}
           a (first more)
           a+ (rest more)]
      (cond
        (vector? a) (assoc m :param-vec a :body a+)
        (map? a) (recur (assoc m :attr-map? a)
                        (first a+) (rest a+))
        (string? a) (recur (assoc m :doc-string? a)
                           (first a+) (rest a+))
        :else (e/throw!-vector-error)))))

(defn condense-keys
  [{:keys [arity keyed-args]}]
  (let [{:keys [optional keys or required]} keyed-args]
    {:arity arity
     :keyed-args keyed-args
     :or or
     :required-keys required
     :optional-keys (vec (union (set optional)
                                (set keys)))
     :all-keys (vec (union (set optional)
                           (set keys)
                           (set required)))}))

(defn parse-arity-and-keyed-args [param-vec]
  (if (vector? param-vec)
    (cond
      (every? symbol? param-vec) {:arity (count param-vec)}
      (and (->> param-vec last map?)
           (->> param-vec butlast last (= '&))
           (->> param-vec butlast butlast (every? symbol?)))
      {:arity (- (count param-vec) 2)
       :keyed-args (last param-vec)}
      :else (e/throw!-param-error))
    (e/throw!-param-error)))

(defn parse-fn-args
  "Given a map with the key `param-vec`, add `required` `optional` and `or`
   to the map by parsing the params in param-vec."
  [{:keys [param-vec] :as arg-map}]
  (merge arg-map
         (-> param-vec
             parse-arity-and-keyed-args
             condense-keys)))

(defn parse-fn-param-vec
  [{:keys [all-keys or param-vec] :as arg-map}]
  (assoc arg-map :fn-param-vec
         (if-let [m (merge (when (seq or) {:or or})
                           (when (seq all-keys) {:keys all-keys}))]
           (conj (vec (butlast param-vec)) m)
           param-vec)))

(defn parse
  [args]
  (-> args
      parse-macro-args
      parse-fn-args
      parse-fn-param-vec))
