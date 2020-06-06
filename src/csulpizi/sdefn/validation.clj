(ns csulpizi.sdefn.validation
  "Validation checks for calls to functions defined by sdefn."
  (:require [csulpizi.sdefn.utils :refer [ks->message missing-keys]]))

(defn throw-exception-when-keys-missing [missing-params]
  (when (seq missing-params)
    (throw (Exception. (str "Missing parameters: " (ks->message missing-params))))))

(defn validate-params
  "params        -> map
   required-keys -> vector of keywords
   Check that <params> contains all keys in <required-keys>.
   If any keys are missing, throw an exception explaining the missing keys."
  [required-keys params]
  {:pre [(vector? required-keys)
         (map? params)
         (every? keyword? required-keys)]}
  (-> (missing-keys required-keys params)
      throw-exception-when-keys-missing))
