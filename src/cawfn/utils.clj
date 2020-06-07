(ns cawfn.utils
  "Some basic utility functions."
  (:require [clojure.set :refer [difference]]))

(defn ks->message
  "[:a :b :c] -> 'a, b, c'"
  [ks]
  (->> (map name ks)
       (clojure.string/join ",")))

(defn missing-keys [ks m]
  (difference (set ks) (-> m keys set)))

(defn unknown-keys [ks m]
  (difference (-> m keys set)
              (set ks)))

(defmacro defmacro*
  "A wrapper for defmacro that expects a doc-string and an attribute-map to be
   provided, but will ignore them if their value is nil."
  [name# doc-string# attribute-map# params# & body#]
  (list* 'defmacro
         (concat [name#] (filter some? [doc-string# attribute-map#]) [params#] body#)))
