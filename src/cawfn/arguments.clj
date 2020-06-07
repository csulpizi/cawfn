(ns cawfn.arguments
  "Handlers for cawfn arguments and paramters.")

(def args-sym '_args)

(defn cawfn-params->defn-params
  "Take the deconstructed values provided in the param vector in cawfn
   and create a param vector that can be passed into defn."
  [required optional or]
  (cond
    (and (empty? required) (empty? optional)) []
    (empty? or) ['& {:keys (vec (concat required optional)) :as args-sym}]
    :else ['& {:keys (vec (concat required optional)) :or or :as args-sym}]))

(defn parse-cawfn-params
  "Given a vector of params passed into cawfn, return a map with keys
  :required-keys#, known-keys#, :defn-params#.
  :required-keys# -> given cawfn-params [:required [...] :optional [...] :or {...}],
                      return the symbols listed in :required's vector as keywords
  :known-keys#    -> return all symbols listed in both :required and :optional
  :defn-params#   -> convert cawfn-params into a param vector suitable for defn,
                      ie. in the form [& {:keys [...] :or {} :as _}]"
  [& {:keys [required optional or]}]
  {:defn-params# (cawfn-params->defn-params required optional or)
   :required-keys# (vec (map keyword required))
   :known-keys# (vec (map keyword (concat required optional)))})

(defn parse-cawfn-args
  "Given a list of arguments passed into cawfn, return a map with keys
   :doc-string# :attribute-map# :cawfn-params# and :body#"
  [result a0 & args]
  (cond
    (vector? a0)
    (assoc result :cawfn-params# a0 :body# args)
    (string? a0)
    (apply parse-cawfn-args (assoc result :doc-string# a0) args)
    (map? a0)
    (apply parse-cawfn-args (assoc result :attribute-map# a0) args)))

(defn cawfn-args->arg-map
  "Given a set of args provided to cawfn, return a map with keys
  :name# :doc-string# :attribute-map# :cawfn-params# :body# :defn-params# and
  required-keys#."
  [& args]
  (let [{:keys [cawfn-params#] :as parsed-args} (apply parse-cawfn-args {} (rest args))]
    (merge {:name# (first args)}
           (apply parse-cawfn-params cawfn-params#)
           parsed-args)))
