# sdefn

A Clojure library that provides a compile-time aware version of `core/defn`. 

## Rationale 

Functions with large arities can be hard to work with. 

Say you have a fn `(defn foo [a b c d e f g h i j k] ...)`

Calling `foo` is a nightmare. Accidentally mixing up the ordering of the arguments is likely.

Similarly, this way of defining functions is not very future proof. Any changes you make to the definition of the function will affect every call of that function, and it can be fairly easy to lost track of all of the calls you need to adjust and make sure they follow the new definition.

This is also an issue that is not very easy to test. Unit tests can test the definition of foo itself, but that doesn't guarantee that every call to foo is written appropriately.

sdefn was written to address this problem. sdefn allows you to define a function in a similar manner to defn, but it wraps the function with a validation function that runs at compile time. The validation checks that all required arguments have been provided, and that no extraneous (or mispelled) arguments are provided. 

## Usage

Usage is fairly straight forward.

`(sdefn foo [:required [a b c] :optional [d e] :or {d 7}] ... <insert body here>`

The above snippet defines a function `foo`. `foo` has 5 arguments: `a`, `b`, `c`, `d`, and `e`. In order to define a call to `foo`, you need to provide it with `a`, `b`, and `c`. If any argument is missing, an exception will be thrown.

`(foo :a 6 :b 7 :c 4 :d 2)`

The above snippet would successfully compile and perform the given body with the provided values.

`(foo :a 2 :b 3)`

The above snippet would throw an exception during compile-time since it does not specify argument `c`.
