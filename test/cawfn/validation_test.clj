(ns cawfn.validation-test
  (:require [clojure.test :refer [deftest is]]
            [cawfn.validation :as v]))

(deftest test-verify-no-missing-keys
  (is (nil? (v/verify-no-missing-keys 'foo [:a :b :c] [:a :b :c :d]))
      "No missing keys returns nil.")
  (is (nil? (v/verify-no-missing-keys 'foo [] [:a :b :c :d]))
      "Having an empty set of required keys succeeds.")
  (is (thrown? Exception (v/verify-no-missing-keys 'foo [:a :b :c] [:a :b]))
      "Missing keys throw exception."))

(deftest test-verify-no-unknown-keys
  (is (nil? (v/verify-no-unknown-keys 'foo [:a :b :c] [:a :b]))
      "Providing less than required keys returns nil.")
  (is (nil? (v/verify-no-unknown-keys 'foo [:a :b :c] [:a :b :c]))
      "Providing exactly the required keys returns nil.")
  (is (thrown? Exception (v/verify-no-unknown-keys 'foo [:a :b :c] [:a :b :c :d]))
      "Providing an extra key throws an exception."))

(deftest test-validate
  (is (nil? (v/validate {:fn-name 'foo
                         :all-keys ['a 'b 'c 'd]
                         :required-keys ['b 'c]
                         :arity 2}
                        [1 2 :a 1 :b 2 :c 3]))
      "Providing required keys but not all keys succeeds.")
  (is (nil? (v/validate {:fn-name 'foo
                         :all-keys ['a 'b 'c 'd]
                         :required-keys ['b 'c]
                         :arity 2}
                        [1 2 :a 1 :b 2 :c 3 :d 4]))
      "Providing all of the keys succeeds.")
  (is (nil? (v/validate {:fn-name 'foo
                         :all-keys ['a 'b 'c 'd]
                         :required-keys ['b 'c]
                         :arity 2}
                        [1 2 :c 1 :a 2 :b 3 :d 4]))
      "Reordering the keys has no effect.")
  (is (thrown? Exception
               (v/validate {:fn-name 'foo
                            :all-keys ['a 'b 'c 'd]
                            :required-keys ['b 'c]
                            :arity 2}
                           [1 2 :c 1 :a 2 :b 3 :d 4 :e 5]))
      "Providing an unknown key throws an exception.")
  (is (thrown? Exception
               (v/validate {:fn-name 'foo
                            :all-keys ['a 'b 'c 'd]
                            :required-keys ['b 'c]
                            :arity 2}
                           [1 2 :c 1 :a 2 :d 4]))
      "Not providing a required key fails.")
  (is (nil? (v/validate {:fn-name 'foo
                         :all-keys ['a 'b 'c 'd]
                         :required-keys []
                         :arity 2}
                        [1 2]))
      "Providing no extras succeeds if nothing is required."))
