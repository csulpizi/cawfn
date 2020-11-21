(ns cawfn.expansion
  (:require [cawfn.validation :refer [validate]]
            [clojure.walk :refer [postwalk]]))

(defn expand-fn
  "Returns a list of the form (fn [a b ...] `body`)"
  [fn-param-vec body]
  (list* `fn fn-param-vec body))

(defn expand-fn-call
  "Returns a list of the form (apply `f` `param-sym`),
   where `f` is a fn defined by `expand-fn`."
  [{:keys [fn-param-vec body param-sym]}]
  [(list `apply (expand-fn fn-param-vec body) param-sym)])

(defn default-arg-list
  ;;NOTE(cs): This is quite possibly the grossest function I've ever written...
  ;;          but it works :shrug:
  "Returns a map of the form {:arglists '(`param-vec`)}
   which can be used as the default arglist for the expanded macro."
  [param-vec]
  {:arglists (list
              list
              (postwalk #(if (symbol? %)
                           (list `symbol (keyword %))
                           %) param-vec))})

(defn expand-macro-args
  "Returns a vector of the form [`fn-name` `doc-string?` `attr-map?` [& `param-sym`]]"
  [{:keys [fn-name doc-string? attr-map? param-sym param-vec] :as args}]
  [fn-name doc-string?
   (merge (default-arg-list param-vec) attr-map?)
   ['& param-sym]])

(defn expand-outer-macro
  "Returns a list of the form
   (defmacro `fn-name` `doc-string?` `attr-map?` [& `param-sym`]),
  filtering out `doc-string?` and `attr-map?` if they are nil."
  [args]
  (->> (expand-macro-args args)
       (filter some?)
       (list* `defmacro)))

(defn friendly-args [{:keys [required-keys optional-keys
                             all-keys fn-name arity]}]
  {:required-keys (mapv keyword required-keys)
   :optional-keys (mapv keyword optional-keys)
   :all-keys (mapv keyword all-keys)
   :fn-name (str fn-name)
   :arity arity})

(defn expand-assertions [{:keys [param-sym] :as args}]
  [(list `validate (friendly-args args) param-sym)])

(defn expand-cawfn
  [{:keys [fn-name doc-string? attr-map? param-vec param-sym body] :as args}]
  (concat (expand-outer-macro args)
          (expand-assertions args)
          (expand-fn-call args)))
