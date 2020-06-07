# cawfn

A Clojure library that provides a compile-time aware version of `clojure.core/defn`. 

`cawfn` is an abbreviation of '*c*ompile-time *aw*are *f*unctio*n*'

## Rationale 

Functions with large arities can be hard to work with. 

Say you have a fn `(defn foo [a b c d e f g h i j k] ...)`

Calling `foo` is a nightmare. Accidentally mixing up the ordering of the arguments is likely, which you may not catch until something goes wrong. You might not provide the right number of arguments, which isn't checked until the function is called during run-time or in testing.

Similarly, this way of defining functions is not very future proof. Any changes you make to the definition of the function will affect every call of that function, and it can be fairly easy to lose track of all of the calls you need to adjust to make sure they follow the new definition.

This is also an issue that is not very easy to test. Unit tests can test the definition of foo itself, but that doesn't guarantee that every call to foo is written appropriately.

Some of these issues can be solved by providing an argument map object. But that does not verify that the object you are supplying contains the arguments you expect it to have or that the arguments are spelled right.

`cawfn` was written to address this problem. `cawfn` allows you to define a function in a similar manner to `defn`, but it wraps the function with a validation that runs at compile-time. The validation checks that every call to the function provides required arguments, and that no extraneous (or mispelled) arguments are provided. 

## Usage

Usage is fairly straight forward.

`(cawfn foo [:required [a b c] :optional [d e] :or {d 7}] ... <insert body here>`

The above snippet defines a function `foo`. `foo` has 5 arguments: `a`, `b`, `c`, `d`, and `e`. In order to define a call to `foo`, you need to provide it with `a`, `b`, and `c`. If any argument is missing, an exception will be thrown at compile-time. If provided with an unknown argument, say `f`, an exception will be thrown at compile-time. 

`(foo :a 6 :b 7 :c 4 :d 2)`

The above snippet would successfully compile and perform the given body with the provided values.

`(foo :a 2 :b 3)`

The above snippet would throw an exception during compile-time since it does not specify argument `c`.

`(foo :a 6 :b 7 :c 4 :f 2)`

The above snippet would throw an exception during compile-time since it does not recognize argument `f`. (This is a feature to prevent accidentally spelling an argument incorrectly).
