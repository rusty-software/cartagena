(ns cartagena.core-test
  (:require [clojure.test :refer :all]
            [cartagena.core :refer :all]))

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

(deftest initialize-player-test
  (testing "Returns a player data structure full of initial state data"
    (let [expected {:name "rusty" :color :black :pirates [-1 -1 -1 -1 -1 -1] :cards []}
          actual (initialize-player {:name "rusty" :color :black})]
      (doseq [k (keys expected)]
        (is (= (k expected) (k actual)) (str "key value mismatch for: " k))))))

(deftest draw-cards-test
  (testing "Drawing cards for a player expands the card collection and reduces the draw pile"
    (let [player {:name "rusty" :color :black :pirates [-1 -1 -1 -1 -1 -1] :cards [:key :hat]}
          draw-pile [:skull :gun :bottle :knife]
          expected {:player (assoc player :cards [:key :hat :skull :gun])
                    :draw-pile [:bottle :knife]}]
      (is (= expected (draw-cards 2 player draw-pile))))))

(deftest update-player!-test
  (let [players [{:name "tanya" :color :orange} {:name "rusty" :color :black}]]
    (testing "Can update values except name"
      (new-game! players)
      (update-player! "rusty" {:color :green :pirates [1 2 3 4 5 6] :cards [:skull :knife :bottle]})
      (let [actual-player (first (filter #(= "rusty" (:name %)) (:players @game-state)))]
        (is (= :green (:color actual-player)))
        (is (= [1 2 3 4 5 6] (:pirates actual-player)))
        (is (= [:skull :knife :bottle] (:cards actual-player)))))))

(defn assert-player-state []
  (is (= 2 (count (:players @game-state))))
  (is (= "tanya" (get-in @game-state [:players 0 :name]))))
(defn assert-card-state []
  (is (= 6 (count (get-in @game-state [:players 0 :cards]))))
  (is (= 90 (count (:draw-pile @game-state))))
  (is (= 0 (count (:discard-pile @game-state)))))
(defn assert-board-state []
  (is (= 36 (count (:board-spaces @game-state)))))

(deftest new-game-test
  (testing "All game state is initialized correctly"
    (let [players [{:name "tanya" :color :orange} {:name "rusty" :color :black}]]
      (new-game! players)
      (testing "Player state is correct"
        (assert-player-state))
      (testing "Card state is correct"
        (assert-card-state))
      (testing "Board state is correct"
        (assert-board-state)))))

#_(deftest discard-card-test
  (testing "Discarding a card adds it to the discard pile"
    (new-game! [{:name "tanya" :color :orange} {:name "rusty" :color :black}])
    (discard! :key)
    (is (= 1 (count (:discard-pile @game-state))))
    (is (= :key (first (:discard-pile @game-state))))))

#_(deftest ^:single play-card-test
  (testing "Playing a card moves the selected pirate to first open space bearing the card's icon"
    (let [board-spaces [:bottle :gun :hat :key :knife :skull :bottle :gun :hat :key :knife :skull]
          player {:name "tanya" :color :orange :pirates [-1 -1 -1 -1 -1 -1] :cards [:hat :key :skull]}
          card :key
          pirate 0
          expected-player {:name "tanya" :color :orange :pirates [3 -1 -1 -1 -1 -1] :cards [:hat :skull]}
          updated-player (play-card player card pirate board-spaces)])))

(deftest player-move-test
  (testing "Player can play card and move pieces")
  (testing "Player can move backward and receives cards")
  (testing "Player without cards must move backward")
  (testing "Player with no available spaces behind cannot move backward")
  (testing "Player with pirate on ship can move that pirate backward"))

(deftest game-over-test
  (testing "Game ends when a player has all pirates on the ship"))