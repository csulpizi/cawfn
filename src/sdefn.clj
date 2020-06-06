(ns sdefn.core
  "Provides an alternative to defn.
   Any time a function defined by sdefn is called in a code base, the provided
   arguments will be checked to make sure the required-keys have been provided.
   This check is performed at compile time, and if the inputs do not match the
   required inputs an exception will be thrown.

   example:
   -----------
   (sdefn foo [:required [a b] :optional [c]] ... )

   (foo :a 5 :b 3) -> this line will compile properly
                           because it has all the required arguments
   (foo :a 5 :c 3) -> this line will throw an exception at compile
                           time because it is missing argument b
   -----------"
  {:author "github.com/csulpizi" :date "2020/06/06" :version "1.0"}

  (:require [sdefn.arguments :refer [args-sym sdefn-args->arg-map]]
            [sdefn.utils :refer [defmacro*]]
            [sdefn.validation :refer [validate-params]]))

(defmacro sdefn*
  "Creates a macro that checks whether or not  expand to (apply f args) if the requi"
  [f# {:keys [name# doc-string# attribute-map# defn-params# required-keys# known-keys#]}]
  (list `defmacro* name# doc-string# attribute-map# defn-params#
        (list `validate-params required-keys# known-keys# args-sym)
        (list 'list* f# (list 'apply 'concat args-sym))))

(defmacro sdefn
  "Create a function that verifies that the correct arguments have been passed in
   at compile time.

   Arguments should be provided as follows:
     name, doc-string?, attribute-map?, params, & body
   
   An example of a valid params format is as follows:
     [:required [a b c] :optional [d] :or {d 4}]"
  [& args#]
  (let [{:keys [defn-params# body#] :as argmap#} (apply sdefn-args->arg-map args#)]
    (list `sdefn* (list* 'fn defn-params# body#) argmap#)))
