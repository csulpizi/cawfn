(ns csulpizi.smart-defn
  "Provides an alternative to defn, sdefn, allowing you to specify required
   arguments, optional arguments, and default arguments. Every call to a
   function defined by sdefn will be checked for required arguments at
   compile time."
  (:require [clojure.set :refer [difference]]))

(def args-sym '_args)

(defn- ks->message
  "[:a :b :c] -> 'a, b, c'"
  [ks]
  (->> (map name ks)
       (clojure.string/join ",")))

(defn throw-missing-params-error [missing-params]
  (throw (Exception. (str "Missing parameters: " (ks->message missing-params)))))

(defn valid-params?
  "params        -> map
   required-keys -> vector of keywords
   Check that <params> contains all keys in <required-keys>.
   If any keys are missing, throw an exception explaining the missing keys."
  [required-keys params]
  {:pre [(vector? required-keys)
         (map? params)
         (every? keyword? required-keys)]}
  (let [missing-params (difference (set required-keys) (-> params keys set))]
    (if (seq missing-params)
      (throw-missing-params-error missing-params)
      true)))

(defn sdefn-params->defn-params
  "Create a "
  [required optional or]
  (cond
    (and (empty? required) (empty? optional))
    []
    (empty? or)
    ['& {:keys (vec (concat required optional)) :as args-sym}]
    :else
    ['& {:keys (vec (concat required optional)) :or or :as args-sym}]))

(defn parse-params [& {:keys [required optional or]}]
  {:converted-params# (sdefn-params->defn-params required optional or)
   :required-keys# (vec (map keyword required))})

(defn defn-args->map* [a0 & args]
  (loop [result {:name# a0}
         args* args]
    (cond
      (-> args* first vector?)
       (assoc result :params# (first args*) :body# (rest args*))
      (-> args* first string?)
       (recur (assoc result :doc-string# (first args*)) (rest args*))
      (-> args* first map?)
       (recur (assoc result :attribute-map# (first args*)) (rest args*)))))

(defn defn-args->map
  "Given a set of args provided to sdefn, return a map with keys
  :name# :doc-string# :attribute-map# :params# :body# :callable-params#
  required-keys#, where callable-params refe"
  [& args]
  (let [{:keys [params#] :as args*} (apply defn-args->map* args)]
    (merge (apply parse-params params#)
           args*)))


(defmacro defmacro*
  "A wrapper for defmacro that expects a doc-string and an attribute-map to be
   provided, but will ignore them if their value is nil."
  [name# doc-string# attribute-map# params# & body#]
  (list* 'defmacro
         (concat [name#] (filter some? [doc-string# attribute-map#]) [params#] body#)))

(defmacro sdefn*
  "Creates a macro that checks whether or not  expand to (apply f args) if the requi"
  [f# {:keys [name# doc-string# attribute-map# callable-params# required-keys#]}]
  (list 'defmacro* name# doc-string# attribute-map# callable-params#
        (list 'when (list `valid-params? required-keys# args-sym)
              (list 'list* f# (list 'apply 'concat args-sym)))))

(defmacro sdefn
  "Alternative to defn.
   Any time a function defined by sdefn is called in the code base, the provided
   arguments will be checked to make sure the required-keys have been provided.
   This check is performed at compile time, and if the inputs do not match the
   required inputs an exception will be thrown.

   Arguments should be provided as follows:
   name, doc-string?, attribute-map?, params, & body

   An example of a valid params format is as follows:
      [:required [a b c] :optional [d] :or {d 4}]

   The above example specifies that keys :a, :b, and :c are required for the function
   being defined, and :d is an optional key with default value 4"
  [& args#]
  (let [{:keys [converted-params# body#] :as argmap#} (apply defn-args->map args#)]
    (list 'sdefn* (list* 'fn converted-params# body#) argmap#)))
