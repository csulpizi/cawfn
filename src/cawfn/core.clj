(ns cawfn.core
  "Provides an alternative to defn.
   Any time a function defined by cawfn is called in a code base, the provided
   arguments will be checked to make sure the required-keys have been provided.
   This check is performed at compile time, and if the inputs do not match the
   required inputs an exception will be thrown. Similarly, if an argument is
   provided that is unrecognized by the function, an exception will be thrown
   at compile-time.

   cawfn supports doc-strings and attribute maps, however it does not support
   pre/post maps.

   example:
   -----------
   (cawfn foo [a & {:required-keys [b] :optional-keys [c] :or {c 2}}] ... )

   (foo 5 :b 3)      -> this line will compile properly
                            since it has all the required arguments.
   (foo 5 :c 3)      -> this line will throw an exception at compile-time
                            since it is missing argument b.
   (foo 5 :b 3 :d 7) -> this line will throw an exception at compile-time
                            since it has an unrecognized argument d.
   -----------"
  {:author "github.com/csulpizi" :current-version "2.0"
   :version-history
   {"1.0" {:date "2020/06/06"
           :changes "Initial project."}
    "2.0" {:data "2020/10/10"
           :changes "Complete overhaul to improve syntax, add features, and simplify."}}}
  (:require [cawfn.arguments :refer [cawfn-args]]
            [cawfn.util :refer [defmacro*]]
            [cawfn.validation :refer [validate-args]]))

(defmacro cawfn
  "Define a function that verifies that the correct arguments have been passed in
   at compile time.
   An example of a valid params format is as follows:
     [a b {:required-keys [a b c] :optional-keys [d] :or {d 4}]"
  {:added "1.0"
   :arglists '([name doc-string? attr-map? [params*] body])}
  [& _args]
  (let [{:keys [fn-name fn-name-str
                doc-string? attr-map?
                args body required-keys
                all-keys arg-sym] :as whatever}
        (cawfn-args _args)]
    (defmacro* fn-name doc-string? attr-map? args
                (if (seq all-keys)
                  `(do (validate-args ~fn-name-str ~all-keys ~required-keys ~arg-sym)
                       ~@body)
                  `(do ~@body)))))
