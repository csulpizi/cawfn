(ns cawfn.validation
  "Defines tests that verify calls to functions defined by cawfn
   have the required arguments."
  (:require [cawfn.errors :as e]
            [clojure.set :refer [difference]]))

(defn verify-no-missing-keys [fn-name required-keys provided-keys]
  (when-let [missing-keys (not-empty (difference (set required-keys)
                                                 (set provided-keys)))]
    (e/throw!-missing-keys-error fn-name missing-keys)))

(defn verify-no-unknown-keys [fn-name all-keys provided-keys]
  (when-let [unknown-keys (not-empty (difference (set provided-keys)
                                                 (set all-keys)))]
    (e/throw!-unknown-keys-error fn-name unknown-keys)))

(defn validate [{:keys [fn-name all-keys required-keys arity]} provided-args]
  (let [provided-keys (->> provided-args (drop arity) (apply hash-map) keys)]
    (verify-no-missing-keys fn-name required-keys provided-keys)
    (verify-no-unknown-keys fn-name all-keys provided-keys)))
