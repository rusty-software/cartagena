(ns cartagena.core-test
  (:require [clojure.test :refer :all]
            [cartagena.core :refer :all]))

(deftest initialize-board-test
  (testing "Returns the right number of spaces as well as icons"
    (let [board (initialize-board)]
      (is (= 36 (count board)))
      (doseq [icon [:bottle :gun :hat :key :knife :skull]]
        (is (= 6 (count (filter #(= icon %) board)))))))
  (testing "Boards are not exactly alike"
    (is (not (= (initialize-board) (initialize-board))))))
