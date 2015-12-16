(ns cartagena.core-test
  (:require [clojure.test :refer :all]
            [cartagena.core :refer :all]))

#_(defn reset-cards! [test-fn]
  (reset! draw-pile [])
  (reset! discard-pile [])
  (test-fn))

#_(use-fixtures :each reset-cards!)

(deftest initialize-board-test
  (testing "Returns the right number of spaces as well as icons"
    (let [board-spaces (initialize-board)]
      (is (= 36 (count board-spaces)))
      (doseq [icon [:bottle :gun :hat :key :knife :skull]]
        (is (= 6 (count (filter #(= icon %) board-spaces)))))))
  (testing "Boards are not exactly alike"
    (is (not (= (initialize-board) (initialize-board))))))

(deftest initialize-cards-test
  (testing "Returns a pile of 102 cards, 17 of each icon"
    (let [cards (initialize-cards)]
      (is (= 102 (count cards)))
      (doseq [icon icons]
        (is (= 17 (count (filter #(= icon %) cards))))))))

(deftest shuffle-cards-test
  (testing "Returns the same count but in different (random) order"
    (let [cards [:key :key :knife :knife :skull :skull :gun :gun :bottle :bottle :hat :hat]
          shuffled (shuffle-cards cards)]
      (is (= (count cards) (count shuffled)))
      (is (not (= cards shuffled))))))

#_(deftest draw-cards!-test
  (testing "Drawing n cards removes them from the draw pile"
    (place-cards!)
    (shuffle-cards!)
    (draw-cards! 6)
    (is (= 96 (count @draw-pile)))))

#_(deftest initialize-player-test
  (testing "Returns a player data structure full of initial state data"
    (place-cards!)
    (shuffle-cards!)
    (let [expected {:name "rusty" :color :black :pirates [-1 -1 -1 -1 -1 -1]}
          actual (initialize-player {:name "rusty" :color :black})]
      (doseq [k (keys expected)]
        (is (= (k expected) (k actual)) (str "key value mismatch for: " k)))
      (is (= 6 (count (:cards actual)))))))

(deftest new-game-test
  (testing "All game state is initialized correctly"
    (let [players [{:name "tanya" :color :orange} {:name "rusty" :color :black}]]
      (new-game! players)
      (testing "Player state is correct"
        (is (= 2 (count (:players @game-state))) "Should be two players in the collection")
        (is (= 0 (:current-player @game-state)) "Active player should be 0")
        (is (= "tanya" (get-in @game-state [:players 0 :name]))) "Tanya should be the first player name")
      (testing "Card state is correct"
        (is (= 6 (count (get-in @game-state [:players 0 :cards]))))
        (is (= 90 (count (:draw-pile @game-state))))
        (is (= 0 (count (:discard-pile @game-state)))))
      (testing "Board state is correct"
        (is (= 36 (count (:board-spaces @game-state))))))))

(deftest ^:single player-move-test
  (testing "Player can play card and move pieces")
  (testing "Player can move backward and receives cards")
  (testing "Player without cards must move backward")
  (testing "Player with no available spaces behind cannot move backward")
  (testing "Player with pirate on ship can move that pirate backward"))

(deftest game-over-test
  (testing "Game ends when a player has all pirates on the ship"))