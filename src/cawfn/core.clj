(ns cawfn.core
  "Defines cawfn, a compile-time aware alternative to defn."
  {:version "2.0"
   :author "Cory Sulpizi | github.com/csulpizi"
   :repository "https://github.com/csulpizi/cawfn"
   :clojars "https://clojars.org/cawfn"
   :last-modified "2020/11/20"
   :change-log {"1.0" "Init repo"
                "2.0" "Much cleaner implementation, more robust"}}
  (:require [cawfn.expansion :refer [expand-cawfn]]
            [cawfn.parsing :refer [parse]]))

(defmacro cawfn
  "Define a function that verifies that the correct arguments have been passed in
   at compile time.
   An example of a valid params format is as follows:
    [a b {:required [a b c] :optional [d] :or {d 4}]"
  {:added "1.0"
   :arglists '([fn-name doc-string? attr-map? [params*] body])}
  [& args]
  (-> args parse expand-cawfn))
