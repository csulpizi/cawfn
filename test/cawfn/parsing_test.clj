(ns cawfn.parsing-test
  (:require [clojure.test :refer [deftest is]]
            [cawfn.parsing :as p]))

(def results<<parse-macro-args {:fn-name 'foo
                                :param-sym 'G_sym
                                :doc-string? "Doc-string"
                                :attr-map? {:attribute "map!"}
                                :param-vec ['p 'a 'r 'a '& 'ms]
                                :body ['(some action) '(some action 2)]})

(deftest test-parse-macro-args
  (with-redefs [gensym (constantly 'G_sym)]
    (is (= (p/parse-macro-args ['foo "Doc-string"
                                {:attribute "map!"}
                                ['p 'a 'r 'a '& 'ms]
                                '(some action)
                                '(some action 2)])
           results<<parse-macro-args))
    (is (= (p/parse-macro-args ['foo {:attribute "map!"}
                                "Doc-string"
                                ['p 'a 'r 'a '& 'ms]
                                '(some action)
                                '(some action 2)])
           results<<parse-macro-args)
        "Swapping the doc-string and attribute-map is irrelevant.")
    (is (= (p/parse-macro-args ['foo "Doc-string"
                                ['p 'a 'r 'a '& 'ms]
                                '(some action)
                                '(some action 2)])
           (dissoc results<<parse-macro-args :attr-map?))
        "Not including attr map is fine.")
    (is (= (p/parse-macro-args ['foo {:attribute "map!"}
                                ['p 'a 'r 'a '& 'ms]
                                '(some action)
                                '(some action 2)])
           (dissoc results<<parse-macro-args :doc-string?))
        "Not including doc-string is fine.")
    (is (= (p/parse-macro-args ['foo
                                ['p 'a 'r 'a '& 'ms]
                                '(some action)
                                '(some action 2)])
           (dissoc results<<parse-macro-args :doc-string? :attr-map?))
        "Not including either is fine.")
    (is (thrown? Exception
                 (p/parse-macro-args ["Doc-string"
                                      {:attribute "map!"}
                                      ['p 'a 'r 'a '& 'ms]
                                      '(some action)
                                      '(some action 2)]))
        "The first argument must be a symbol.")
    (is (thrown? Exception
                 (p/parse-macro-args ['foo
                                      "Doc-string"
                                      {:attribute "map!"}
                                      '(some action)
                                      '(some action 2)]))
        "There must be a param vector.")))

(deftest test-condense-keys
  (is (= (p/condense-keys {:arity 2
                           :keyed-args {:optional ['a 'b 'c]
                                        :or {'d 'e}
                                        :required ['f 'g]}})
         {:arity 2
          :keyed-args {:optional ['a 'b 'c]
                       :or {'d 'e}
                       :required ['f 'g]}
          :or {'d 'e}
          :required-keys ['f 'g]
          :optional-keys ['a 'c 'b]
          :all-keys ['a 'c 'g 'b 'f]}))
  (is (= (p/condense-keys {:arity 2
                           :keyed-args {:keys ['a 'b 'c]
                                        :or {'d 'e}
                                        :required ['f 'g]}})
         {:arity 2
          :keyed-args {:keys ['a 'b 'c]
                       :or {'d 'e}
                       :required ['f 'g]}
          :or {'d 'e}
          :required-keys ['f 'g]
          :optional-keys ['a 'c 'b]
          :all-keys ['a 'c 'g 'b 'f]})
      ":keys and :optional are aliases")
  (is (= (p/condense-keys {:arity 2
                            :keyed-args {:keys ['a 'b]
                                         :optional ['c]
                                         :or {'d 'e}
                                         :required ['f 'g]}})
          {:arity 2
           :keyed-args {:keys ['a 'b]
                        :optional ['c]
                        :or {'d 'e}
                        :required ['f 'g]}
           :or {'d 'e}
           :required-keys ['f 'g]
           :optional-keys ['a 'c 'b]
           :all-keys ['a 'c 'g 'b 'f]})
      ":keys and :optional are merged if provided both")
  (is (= (p/condense-keys {:arity 2})
         {:arity 2
          :keyed-args nil
          :or nil
          :required-keys nil
          :optional-keys []
          :all-keys []})
      "Keyed args may be null"))

(deftest test-parse-arity-and-keyed-args
  (is (= (p/parse-arity-and-keyed-args [])
         {:arity 0}))
  (is (= (p/parse-arity-and-keyed-args ['a 'b 'c])
         {:arity 3}))
  (is (= (p/parse-arity-and-keyed-args ['& {:keys ['d 'e]}])
         {:arity 0
          :keyed-args {:keys ['d 'e]}}))
  (is (= (p/parse-arity-and-keyed-args ['a 'b 'c '& {:keys ['d 'e]}])
         {:arity 3
          :keyed-args {:keys ['d 'e]}}))
  (is (thrown? Exception (p/parse-arity-and-keyed-args ['a "BOO" 'c]))
      "One of the params is unexpectedly not a symbol.")
  (is (thrown? Exception (p/parse-arity-and-keyed-args ['a "BOO" 'c '& {:keys ['d 'e]}]))
      "One of the params is unexpectedly not a symbol.")
  (is (thrown? Exception (p/parse-arity-and-keyed-args ['a "BOO" 'c 'd {:keys ['d 'e]}]))
      "Missing the & symbol to indicate there's a map to follow.")
  (is (thrown? Exception (p/parse-arity-and-keyed-args nil))
      "This param list is not even a vector."))

(deftest test-parse-fn-args
  (let [input (assoc results<<parse-macro-args
                     :param-vec
                     ['a 'b 'c '& {:optional ['a 'b 'c]
                                   :required ['d 'e]
                                   :or {'b 90}}])]
    (is (= (p/parse-fn-args input)
           (assoc input :arity 3
                  :keyed-args {:optional ['a 'b 'c]
                               :required ['d 'e]
                               :or {'b 90}}
                  :or {'b 90}
                  :required-keys ['d 'e]
                  :optional-keys ['a 'c 'b]
                  :all-keys ['a 'e 'c 'b 'd]))))
  (let [input (assoc results<<parse-macro-args
                     :param-vec ['a 'b 'c])]
    (is (= (p/parse-fn-args input)
           (assoc input :arity 3
                  :keyed-args nil
                  :or nil
                  :required-keys nil
                  :optional-keys []
                  :all-keys []))))
  (let [input (assoc results<<parse-macro-args
                     :param-vec
                     ['& {:optional ['a 'b 'c]
                          :required ['d 'e]
                          :or {'b 90}}])]
    (is (= (p/parse-fn-args input)
           (assoc input :arity 0
                  :keyed-args {:optional ['a 'b 'c]
                               :required ['d 'e]
                               :or {'b 90}}
                  :or {'b 90}
                  :required-keys ['d 'e]
                  :optional-keys ['a 'c 'b]
                  :all-keys ['a 'e 'c 'b 'd]))))
  (let [input (assoc results<<parse-macro-args
                     :param-vec
                     [])]
    (is (= (p/parse-fn-args input)
           (assoc input :arity 0
                  :keyed-args nil
                  :or nil
                  :required-keys nil
                  :optional-keys []
                  :all-keys []))))
  (let [input (assoc results<<parse-macro-args
                     :param-vec ['a "HEY WHAT ARE YOU DOING"])]
    (is (thrown? Exception (p/parse-fn-args input))
        "Param vec incorrectly specified.")))

(deftest test-parse-fn-param-vec
  (let [input {:all-keys ['d 'e 'f] :or {'e 3} :param-vec ['a 'b 'c '& {:what :ever}]}]
    (is (= (assoc input :fn-param-vec '[a b c & {:or {e 3} :keys [d e f]}])
           (p/parse-fn-param-vec input))))
  (let [input {:param-vec ['a 'b 'c]}]
    (is (= (assoc input :fn-param-vec '[a b c])
           (p/parse-fn-param-vec input))))
  (let [input {:param-vec []}]
    (is (= (assoc input :fn-param-vec [])
           (p/parse-fn-param-vec input)))))

(deftest test-parse
  (with-redefs [gensym (constantly 'G__sym)]
    (is (= '{:arity 3
             :keyed-args {:optional [e f g] :required [h i] :or {f 3 g 2}}
             :or {f 3 g 2}
             :required-keys [h i]
             :optional-keys [e g f]
             :all-keys [i e g h f]
             :fn-name foo
             :doc-string? "Doc-string"
             :attr-map? {:attribute "map!"}
             :body ((some action) (some action 2))
             :param-vec [a b c & {:optional [e f g] :required [h i] :or {f 3 g 2}}]
             :param-sym G__sym
             :fn-param-vec [a b c & {:or {f 3 g 2} :keys [i e g h f]}]}
           (p/parse ['foo
                     "Doc-string"
                     {:attribute "map!"}
                     '[a b c & {:optional [e f g] :required [h i] :or {f 3 g 2}}]
                     '(some action)
                     '(some action 2)])))))
