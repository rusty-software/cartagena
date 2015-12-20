(ns cartagena.core-test
  (:require [clojure.test :refer :all]
            [cartagena.core :refer :all]))

(deftest initialize-board-test
  (testing "Returns the right number of spaces as well as icons"
    (let [expected-jail {:icon :jail :pirates []}
          expected-ship {:icon :ship :pirates []}
          board-spaces (initialize-board)
          actual-jail (first board-spaces)
          actual-ship (last board-spaces)
          player-spaces (butlast (rest board-spaces))]
      (is (= expected-jail actual-jail))
      (is (= expected-ship actual-ship))
      (is (= 36 (count player-spaces)))
      (doseq [space player-spaces]
        (is (not (nil? (:pirates space))))
        (is (empty? (:pirates space)))
        (is (some #{(:icon space)} icons)))
      (doseq [icon icons]
        (is (= 6 (count (filter #(= icon (:icon %)) player-spaces)))))))
  ;; fragile test follows: it's possible that three draws in a row would be the same...
  (testing "Boards are not exactly alike"
    (is (not (= (initialize-board) (initialize-board) (initialize-board))))))

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
    (let [expected {:name "rusty" :color :black :cards []}
          actual (initialize-player {:name "rusty" :color :black})]
      (doseq [k (keys expected)]
        (is (= (k expected) (k actual)) (str "key value mismatch for: " k))))))

(deftest draw-cards-test
  (testing "Drawing cards for a player expands the card collection and reduces the draw pile"
    (let [player {:name "rusty" :color :black :cards [:key :hat]}
          draw-pile [:skull :gun :bottle :knife]
          discard-pile [:bottle :gun :hat :key :knife :skull]
          expected {:player (assoc player :cards [:key :hat :skull :gun])
                    :draw-pile [:bottle :knife]
                    :discard-pile discard-pile}]
      (is (= expected (draw-cards 2 player draw-pile discard-pile)))))
  (testing "When the draw pile doesn't have enough, discard is shuffled into the draw pile"
    (let [player {:name "rusty" :color :black :cards [:key :hat]}
          draw-pile [:skull]
          discard-pile [:gun :bottle :knife]
          certain-cards #{:key :hat :skull}
          actual (draw-cards 2 player draw-pile discard-pile)]
      (doseq [card certain-cards]
        (is (filter #(= card %) (:cards player))))
      (is (= 2 (count (:draw-pile actual)))))))

(defn assert-player-state []
  (is (= 2 (count (:players @game-state))))
  (is (= "tanya" (get-in @game-state [:players 0 :name]))))
(defn assert-card-state []
  (is (= 6 (count (get-in @game-state [:players 0 :cards]))))
  (is (= 90 (count (:draw-pile @game-state))))
  (is (= 0 (count (:discard-pile @game-state)))))
(defn assert-board-state []
  (is (= 38 (count (:board-spaces @game-state)))))

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

(deftest is-open-target?-test
  (is (is-open-target? {:icon :hat :pirates [:orange :orange]} :hat))
  (is (not (is-open-target? {:icon :bottle :pirates []} :hat)))
  (is (not (is-open-target? {:icon :hat :pirates [:orange :black :green]} :hat))))

(deftest open-space-index-test
  (let [space-index 1
        icon :skull
        board [{:icon :bottle :pirates [:orange]}
               {:icon :hat :pirates [:black :orange]}
               {:icon :knife :pirates []}
               {:icon :bottle :pirates [:black]}
               {:icon :skull :pirates []}
               {:icon :skull :pirates [:black]}
               {:icon :ship :pirates []}]]
    (testing "Given a starting index, board, and icon, finds the first open space after the starting index"
      (is (= 4 (open-space-index space-index board icon))))
    (testing "Spaces with three pirates are skipped"
      (let [board (assoc board 4 {:icon :skull :pirates [:black :black :black]})]
        (is (= 5 (open-space-index space-index board icon)))))
    (testing "If no spaces are available, advance to ship"
      (let [board (assoc board 4 {:icon :skull :pirates [:black :black :black]}
                               5 {:icon :skull :pirates [:black :black :black]})]
        (is (= 6 (open-space-index space-index board icon)))))))

(deftest play-card-test
  (testing "Moves the player's pirate from a space to the next space with the card's icon"
    (let [player {:name "tanya" :color :orange :cards [:hat :skull :knife]}
          icon :skull
          from-space {:icon :hat :pirates [:black :orange]}
          board [{:icon :bottle :pirates [:orange]}
                 from-space
                 {:icon :knife :pirates []}
                 {:icon :bottle :pirates [:black]}
                 {:icon :skull :pirates []}
                 {:icon :skull :pirates [:black]}]
          discard-pile [:gun :key]
          updated-spaces [{:icon :bottle :pirates [:orange]}
                          {:icon :hat :pirates [:black]}
                          {:icon :knife :pirates []}
                          {:icon :bottle :pirates [:black]}
                          {:icon :skull :pirates [:orange]}
                          {:icon :skull :pirates [:black]}]
          expected {:player {:cards [:hat :knife]}
                    :board-spaces updated-spaces
                    :discard-pile [:gun :key :skull]}]
      (is (= expected (play-card player icon from-space board discard-pile))))))

(deftest move-back-test
  (testing "Can move back to the first space with one or two pirates"
    (let [player {:name "tanya" :color :orange :cards [:hat :skull :knife]}
          from-space {:icon :hat :pirates [:black :orange]}
          board [{:icon :bottle :pirates [:orange]}
                 {:icon :knife :pirates []}
                 {:icon :bottle :pirates [:black]}
                 {:icon :skull :pirates []}
                 from-space
                 {:icon :skull :pirates [:black]}]
          draw-pile [:gun :key]
          updated-spaces [{:icon :bottle :pirates [:orange]}
                          {:icon :knife :pirates []}
                          {:icon :bottle :pirates [:black :orange]}
                          {:icon :skull :pirates []}
                          {:icon :hat :pirates [:black]}
                          {:icon :skull :pirates [:black]}]
          expected {:player {:cards [:hat :knife]}
                    :board-spaces updated-spaces
                    :draw-pile [:key]}]
      (is (= expected (move-back player from-space board draw-pile)))))
  (testing "Skips over spaces with three pirates")
  (testing "Does not allow moving back to jail"))

;(deftest discard-card-test
;  (testing "Discarding a card adds it to the discard pile"
;    (new-game! [{:name "tanya" :color :orange} {:name "rusty" :color :black}])
;    (discard! :key)
;    (is (= 1 (count (:discard-pile @game-state))))
;    (is (= :key (first (:discard-pile @game-state))))))

;(deftest ^:single play-card-test
;  (testing "Playing a card moves the selected pirate to first open space bearing the card's icon"
;    (let [board-spaces [:bottle :gun :hat :key :knife :skull :bottle :gun :hat :key :knife :skull]
;          player {:name "tanya" :color :orange :pirates [-1 -1 -1 -1 -1 -1] :cards [:hat :key :skull]}
;          card :key
;          pirate 0
;          expected-player {:name "tanya" :color :orange :pirates [3 -1 -1 -1 -1 -1] :cards [:hat :skull]}
;          updated-player (play-card player card pirate board-spaces)])))

(deftest player-move-test
  (testing "Player can play card and move pieces")
  (testing "Player can move backward and receives cards")
  (testing "Player without cards must move backward")
  (testing "Player with no available spaces behind cannot move backward")
  (testing "Player with pirate on ship can move that pirate backward"))

(deftest game-over-test
  (testing "Game ends when a player has all pirates on the ship"))