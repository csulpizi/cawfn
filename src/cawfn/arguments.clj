(ns cawfn.arguments
  (:require [cawfn.util :refer [vcat]]))

(defn &map? [args]
  (and (-> args last map?)
       (-> args butlast last (= '&))))

(defn update-args [arg-sym args]
  (if (&map? args)
    (let [m (last args)
          m* (-> m
                 (dissoc :optional-keys)
                 (dissoc :required-keys)
                 (update :keys vcat (:optional-keys m))
                 (update :keys vcat (:required-keys m))
                 (assoc :as arg-sym))]
      (-> args butlast (vcat [m*])))
    args))

(defn fetch-required-keys [args]
  (when (&map? args)
    (->> args last :required-keys (map keyword) vec)))

(defn fetch-all-keys [args]
  (when (&map? args)
    (vcat (->> args last :required-keys (map keyword))
          (->> args last :optional-keys (map keyword))
          (->> args last :keys (map keyword)))))

(defn parse-args [args]
  (loop [result {:fn-name (first args)}
         args-left (rest args)
         conditions-left [[:doc-string? string?]
                          [:attr-map? map?]
                          [:params vector?]]]
    (if-let [[k pred] (first conditions-left)]
      (if (-> args-left first pred)
        (recur (assoc result k (first args-left))
          (rest args-left)
          (rest conditions-left))
        (recur result
          args-left
          (rest conditions-left)))
      (assoc result :body args-left))))

(defn cawfn-args [args]
  (let [arg-sym (gensym)
        {:keys [fn-name params]
         :as parsed-args} (parse-args args)]
    (merge parsed-args
           {:fn-name-str (name fn-name)
            :required-keys (fetch-required-keys params)
            :all-keys (fetch-all-keys params)
            :arg-sym arg-sym
            :args (update-args arg-sym params)})))
