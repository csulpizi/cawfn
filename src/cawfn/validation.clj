(ns cawfn.validation
  (:require [clojure.set :refer [difference]]))

(defn missing-keys [required-keys given-keys]
  (difference (set required-keys)
              (set given-keys)))

(defn unknown-keys [defined-keys given-keys]
  (difference (set given-keys)
              (set defined-keys)))

(defn throw-missing-keys-error [name-str missing]
  (throw (Exception. (str "Function call to " name-str " is missing the following required arguments: " missing))))

(defn throw-unknown-keys-error [name-str unknown]
  (throw (Exception. (str "Function call to " name-str " has unknown arguments: " unknown))))

(defn validate-args [name-str all-keys required-keys args]
  (let [missing (missing-keys required-keys (keys args))
        unknown (unknown-keys all-keys (keys args))]
    (cond (seq missing) (throw-missing-keys-error name-str missing)
          (seq unknown) (throw-unknown-keys-error name-str unknown))))
