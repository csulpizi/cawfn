(ns csulpizi.sdefn.arguments
  "Handlers for sdefn arguments and paramters.")

(def args-sym '_args)

(defn sdefn-params->defn-params
  "Take the deconstructed values provided in the param vector in sdefn
   and create a param vector that can be passed into defn."
  [required optional or]
  (cond
    (and (empty? required) (empty? optional)) []
    (empty? or) ['& {:keys (vec (concat required optional)) :as args-sym}]
    :else ['& {:keys (vec (concat required optional)) :or or :as args-sym}]))

(defn parse-sdefn-params
  "Given a vector of params passed into sdefn, return a map with keys
  :required-keys# and :defn-params#.
  :required-keys# -> given sdefn-params [:required [...] :optional [...] :or {...}],
                      return the symbols listed in :required's vector as keywords
  :defn-params#   -> convert sdefn-params into a param vector suitable for defn,
                      ie. in the form [& {:keys [...] :or {} :as _}]"
  [& {:keys [required optional or]}]
  {:defn-params# (sdefn-params->defn-params required optional or)
   :required-keys# (vec (map keyword required))})

(defn parse-sdefn-args
  "Given a list of arguments passed into sdefn, return a map with keys
   :doc-string# :attribute-map# :sdefn-params# and :body#"
  [result a0 & args]
  (cond
    (vector? a0)
    (assoc result :sdefn-params# a0 :body# args)
    (string? a0)
    (apply parse-sdefn-args (assoc result :doc-string# a0) args)
    (map? a0)
    (apply parse-sdefn-args (assoc result :attribute-map# a0) args)))

(defn sdefn-args->arg-map
  "Given a set of args provided to sdefn, return a map with keys
  :name# :doc-string# :attribute-map# :sdefn-params# :body# :defn-params# and
  required-keys#."
  [& args]
  (let [{:keys [sdefn-params#] :as parsed-args} (apply parse-sdefn-args {} (rest args))]
    (merge {:name# (first args)}
           (apply parse-sdefn-params sdefn-params#)
           parsed-args)))
