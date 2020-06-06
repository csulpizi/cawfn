(ns sdefn.validation
  "Validation checks for calls to functions defined by sdefn."
  (:require [sdefn.utils :refer [ks->message missing-keys unknown-keys]]))

(defn throw-exception-when-keys-missing [missing-params]
  (when (seq missing-params)
    (throw (Exception. (str "Missing parameters: " (ks->message missing-params) ". You must provide all required parameters.")))))

(defn throw-exception-when-keys-unknown [unknown-params]
  (when (seq unknown-params)
    (throw (Exception. (str "Unknown keys: " (ks->message unknown-params) ". These parameters are not defined in the called function. Check that this parameter is spelled right, or add the parameter to the called function.")))))

(defn validate-params
  "params        -> map
   required-keys -> vector of keywords
   Check that <params> contains all keys in <required-keys>.
   If any keys are missing, throw an exception explaining the missing keys."
  [required-keys known-keys params]
  {:pre [(vector? required-keys)
         (vector? known-keys)
         (map? params)
         (every? keyword? required-keys)
         (every? keyword? known-keys)]}
  (-> (missing-keys required-keys params)
      throw-exception-when-keys-missing)
  (-> (unknown-keys known-keys params)
      throw-exception-when-keys-unknown))
