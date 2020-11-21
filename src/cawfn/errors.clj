(ns cawfn.errors
  "Defines exceptions for cawfn lib."
  (:require [clojure.set :refer [intersection]]))

(defn throw!-name-error []
  (throw (Exception. "cawfn expansion failed. First argument must be a simple symbol.")))

(defn throw!-vector-error []
  (throw (Exception. "cawfn expansion failed. Vector arglist required but not given.")))

(defn throw!-param-error []
  (throw (Exception. (str "cawfn expansion failed. Parameter list must be a vector"
                          " containing either all simple symbols and / or the last two"
                          " entries be a map preceded by the & symbol."))))

(defn throw!-missing-keys-error [fn-name missing-keys]
  (throw (Exception. (str "Call to cawfn function "
                          fn-name
                          " failed."
                          " Required arguments were not provided: "
                          missing-keys))))

(defn throw!-unknown-keys-error [fn-name unknown-keys]
  (throw (Exception. (str "Call to cawfn function "
                          fn-name
                          " failed."
                          " Unknown-arguments were provided: "
                          unknown-keys))))
