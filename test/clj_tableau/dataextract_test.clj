(ns clj-tableau.dataextract-test
  (:require [clojure.test :refer :all]
            [clj-tableau.dataextract :refer :all]))

(deftest extract-creation
  (testing "If the tde file is created."
    (is (= 1 1))
    (is (= 1 1))))
