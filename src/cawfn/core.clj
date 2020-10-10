(ns cawfn.core
  "Provides an alternative to defn.
   Any time a function defined by cawfn is called in a code base, the provided
   arguments will be checked to make sure the required-keys have been provided.
   This check is performed at compile time, and if the inputs do not match the
   required inputs an exception will be thrown. Similarly, if an argument is
   provided that is unrecognized by the function, an exception will be thrown
   at compile-time.

   example:
   -----------
   (cawfn foo [:required [a b] :optional [c]] ... )

   (foo :a 5 :b 3)      -> this line will compile properly
                            since it has all the required arguments.
   (foo :a 5 :c 3)      -> this line will throw an exception at compile-time
                            since it is missing argument b.
   (foo :a 5 :b 3 :d 7) -> this line will throw an exception at compile-time
                            since it has an unrecognized argument d.
   -----------"
  {:author "github.com/csulpizi" :date "2020/06/06" :version "1.0"}

  (:require [cawfn.arguments :refer [args-sym cawfn-args->arg-map]]
            [cawfn.utils :refer [defmacro*]]
            [cawfn.validation :refer [validate-params]]))

(defmacro cawfn*
  "Creates a macro that checks whether or not the right arguments have been provided.
   If so, it expands into a call to f with the given arguments."
  [f# {:keys [name# doc-string# attribute-map# defn-params# required-keys# known-keys#]}]
  (list `defmacro* name# doc-string# attribute-map# defn-params#
        (list `validate-params required-keys# known-keys# args-sym)
        (list 'list* f# (list 'apply 'concat args-sym))))

(defmacro cawfn
  "Create a function that verifies that the correct arguments have been passed in
   at compile time.

   Arguments should be provided as follows:
     name, doc-string?, attribute-map?, params, & body
   
   An example of a valid params format is as follows:
     [:required [a b c] :optional [d] :or {d 4}]"
  [& args#]
  (let [{:keys [defn-params# body#] :as argmap#} (apply cawfn-args->arg-map args#)]
    (list `cawfn* (list* 'fn defn-params# body#) argmap#)))
