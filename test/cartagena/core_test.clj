(ns cartagena.core-test
  (:require [clojure.test :refer :all]
            [cartagena.core :refer :all]))

(defn reset-cards! [test-fn]
  (reset! draw-pile [])
  (reset! discard-pile [])
  (test-fn))

(use-fixtures :each reset-cards!)

(deftest initialize-board!-test
  (testing "Returns the right number of spaces as well as icons"
    (let [board (initialize-board!)]
      (is (= 36 (count board)))
      (doseq [icon [:bottle :gun :hat :key :knife :skull]]
        (is (= 6 (count (filter #(= icon %) board)))))))
  (testing "Boards are not exactly alike"
    (is (not (= (initialize-board!) (initialize-board!))))))

(deftest place-cards!-test
  (testing "Placing cards populates the discard pile"
    (is (= 0 (count @discard-pile)))
    (place-cards!)
    (is (= 102 (count @discard-pile)))))

(deftest shuffle-cards!-test
  (testing "Shuffles the cards in the discard pile and places them in the draw pile"
    (place-cards!)
    (is (= 0 (count @draw-pile)))
    (let [discards (vec @discard-pile)]
      (shuffle-cards!)
      (is (= 102 (count @draw-pile)))
      (is (= 0 (count @discard-pile)))
      (is (not (= discards @draw-pile)))))
  (testing "Shuffling cards when the draw pile is empty does nothing"
    (place-cards!)
    (reset! draw-pile @discard-pile)
    (reset! discard-pile [])
    (is (= 0 (count @discard-pile)))
    (is (= 102 (count @draw-pile)))
    (let [draws @draw-pile]
      (shuffle-cards!)
      (is (= draws @draw-pile)))))

(deftest initialize-player-test
  (testing "Returns a player data structure full of initial state data")
  (testing "Each player should have six pirates")
  (testing "Each player should have six cards"))

(deftest new-game-test
  (testing "Player count should be equal to the number to which it was initialized")
  (testing "All players should be in jail")
  (testing "Active player should be player 1"))

(deftest player-move-test
  (testing "Player can play card and move pieces")
  (testing "Player can move backward and receives cards")
  (testing "Player without cards must move backward")
  (testing "Player with no available spaces behind cannot move backward")
  (testing "Player with pirate on ship can move that pirate backward"))

(deftest game-over-test
  (testing "Game ends when a player has all pirates on the ship"))