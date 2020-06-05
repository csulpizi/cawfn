(ns defn+.core
  (:require [clojure.set :refer [difference]]))

;;Utils

(defn nil-or-pred?
  [pred x]
  (or (nil? x)
      (pred x)))

(defn update-nth
  [v n f & args]
  {:pre [(vector? v)
         (nat-int? n)
         (fn? f)]}
  (conj (when (-> n dec nat-int?) (subvec v 0 (dec n)))
        (apply f (nth v n) args)
        (when (< (inc n) (count v))                         ;;FIXME(cs) This is awkward
          (subvec v (inc n)))))

;;Functions during definitions

(defn symbol->symbol$_ [sym]
  (-> sym name (str "$_") symbol))

(defn macro-params->fn-params
  [{:keys [required optional as or]}]
  {:pre [(nil-or-pred? vector? required)
         (every? symbol? required)
         (nil-or-pred? vector? optional)
         (every? symbol? optional)
         (nil-or-pred? symbol? as)
         (nil-or-pred? map? or)]}
  (-> {:keys (conj required optional)}
      (assoc :as as :or or)
      vector))

(defn args->params+new-args [& args]                        ;;FIXME(cs): Make this iterative
  (println (nth args 0))
  (cond
    (vector? (nth args 0)) {:params (nth args 0) :new-args (update-nth (nth args 0) 0 macro-params->fn-params)}
    (vector? (nth args 1)) {:params (nth args 1) :new-args (update-nth (nth args 1) 1 macro-params->fn-params)}
    (vector? (nth args 2)) {:params (nth args 2) :new-args (update-nth (nth args 2) 2 macro-params->fn-params)}
    :else (throw (Exception. "defn++ or whatever did not conform to spec")))) ;;FIXME(cs)

(defn valid-params? [required-keys params]
  {:pre [(vector? required-keys)
         (vector? params)
         (every? keyword? required-keys)
         (-> params count even?)]}
  (empty? (difference (set required-keys)
                      (-> (apply hash-map params)
                          keys
                          set))))

(defmacro defn+ [name# & args#]
  (let [name*# (symbol->symbol$_ name#)
        {:keys [params# args*#]} (apply args->params+new-args args#)]
    (list 'defn name*# args*#)
    (println "COOLIO")
    (list 'defmacro name# '[args]
          (list 'when
                (list 'valid-params? (:required params#) 'args)
                (list name*# 'args)))))

;;Functions during calls









#_(defn macroize-name [&symbol]
  (-> &symbol symbol name (str "#") symbol))

#_(defmacro run-dis-fn
  [&name &name# &arg-map &required-symbols]
  (println "baaaa" &name)
  (println &arg-map)
  (println (keys &arg-map))
  (println &required-symbols)
  (let [missing-args (difference (-> &arg-map keys set)
                                 (set (map keyword &required-symbols)))]
    (when (seq missing-args)
      (throw (Exception. (str "Arity exception: " &name " is missing arguments " missing-args))))
    (list* &name#
           (for [&arg &required-symbols]
             (get &arg-map (keyword &arg))))))

#_(defmacro create-dis-fn
  [&name &name* &required-args]
  (list 'defmacro &name '[arg-map]


        )
  )

#_(defmacro defn+
  [&name &args & &body]
  (let [&name* (macroize-name &name)
        &required-args (get &args :required)]
    (list* `defn &name* &required-args &body)
    (println "Wallyyyyy" &name)
    (println `(list* run-dis-fn ~&name ~&name* &arg-map ~&required-args))
    (println '[&arg-map])
    (list `defmacro &name '[&arg-map]
          `(list 'run-dis-fn &name &name* &arg-map &required-args))
           #_('list* 'run-dis-fn &name &name* 'arg-map &required-args)
    ))

#_(defn+ example {:required [a b c]
                :optional [d]
                :as args}
       (println "a" a "b" b "bananas")
       (+ a b c))