(ns cartagena.core-test
  (:require [clojure.test :refer :all]
            [cartagena.core :refer :all]))

(deftest initialize-board-test
  (testing "Returns the right number of spaces as well as icons"
    (let [expected-jail {:index 0 :icon :jail :pirates []}
          expected-ship {:index 37 :icon :ship :pirates []}
          board (initialize-board)
          actual-jail (first board)
          actual-ship (last board)
          player-spaces (butlast (rest board))]
      (is (= expected-jail actual-jail))
      (is (= expected-ship actual-ship))
      (is (= 36 (count player-spaces)))
      (doseq [space player-spaces]
        (is (not (nil? (:pirates space))))
        (is (empty? (:pirates space)))
        (is (some #{(:icon space)} icons))
        (is (> (:index space) 0)))
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
  (is (= 38 (count (:board @game-state))))
  (is (= 12 (count (:pirates (first (:board @game-state))))) "Should have 12 pirates in jail"))

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
          expected {:player (assoc player :cards [:hat :knife])
                    :board updated-spaces
                    :discard-pile [:gun :key :skull]}]
      (is (= expected (play-card player icon from-space board discard-pile))))))

(deftest occupied-space-index-test
  (let [space-index 5
        board [{:icon :bottle :pirates [:orange]}
               {:icon :hat :pirates [:black]}
               {:icon :knife :pirates []}
               {:icon :bottle :pirates [:black]}
               {:icon :skull :pirates []}
               {:icon :skull :pirates [:black :orange]}
               {:icon :ship :pirates []}]]
    (testing "Given a starting index and board, finds the first occupied space prior to the starting index"
      (is (= 3 (occupied-space-index space-index board))))
    (testing "Spaces with three pirates are skipped"
      (let [board (assoc board 3 {:icon :skull :pirates [:black :black :black]})]
        (is (= 1 (occupied-space-index space-index board)))))
    (testing "If no spaces are available, returns nil"
      (let [board (assoc board 3 {:icon :skull :pirates []}
                               1 {:icon :skull :pirates []}
                               0 {:icon :skull :pirates []})]
        (is (nil? (occupied-space-index space-index board)))))))

(deftest move-back-test
  (let [player {:name "tanya" :color :orange :cards [:hat :skull :knife]}
        board [{:icon :jail :pirates []}
               {:icon :bottle :pirates [:orange]}
               {:icon :knife :pirates []}
               {:icon :bottle :pirates [:black]}
               {:icon :skull :pirates []}
               {:icon :hat :pirates [:black :orange]}
               {:icon :skull :pirates [:black]}]
        from-space {:icon :hat :pirates [:black :orange]}
        draw-pile [:gun :key]
        discard-pile [:knife]]
    (testing "Can move back to the first space with one or two pirates"
      (let [updated-spaces [{:icon :jail :pirates []}
                            {:icon :bottle :pirates [:orange]}
                            {:icon :knife :pirates []}
                            {:icon :bottle :pirates [:black :orange]}
                            {:icon :skull :pirates []}
                            {:icon :hat :pirates [:black]}
                            {:icon :skull :pirates [:black]}]
            expected {:player (assoc player :cards [:hat :skull :knife :gun])
                      :board updated-spaces
                      :draw-pile [:key]
                      :discard-pile [:knife]}]
        (is (= expected (move-back player from-space board draw-pile discard-pile)))))
    (testing "Skips over spaces with three pirates"
      (let [board (assoc board 3 {:icon :bottle :pirates [:black :black :black]})
            updated-spaces [{:icon :jail :pirates []}
                            {:icon :bottle :pirates [:orange :orange]}
                            {:icon :knife :pirates []}
                            {:icon :bottle :pirates [:black :black :black]}
                            {:icon :skull :pirates []}
                            {:icon :hat :pirates [:black]}
                            {:icon :skull :pirates [:black]}]
            expected {:player (assoc player :cards [:hat :skull :knife :gun])
                      :board updated-spaces
                      :draw-pile [:key]
                      :discard-pile [:knife]}]
        (is (= expected (move-back player from-space board draw-pile discard-pile)))))
    (testing "Does not allow moving back to jail"
      (let [board (assoc board 3 {:icon :bottle :pirates [:black :black :black]}
                               1 {:icon :bottle :pirates []})]
        (is (nil? (move-back player from-space board draw-pile discard-pile)))))
    (testing "Draws two cards for landing on space with two pirates"
      (let [board (assoc board 3 {:icon :bottle :pirates [:black :black]})
            updated-spaces [{:icon :jail :pirates []}
                            {:icon :bottle :pirates [:orange]}
                            {:icon :knife :pirates []}
                            {:icon :bottle :pirates [:black :black :orange]}
                            {:icon :skull :pirates []}
                            {:icon :hat :pirates [:black]}
                            {:icon :skull :pirates [:black]}]
            expected {:player (assoc player :cards [:hat :skull :knife :gun :key])
                      :board updated-spaces
                      :draw-pile []
                      :discard-pile [:knife]}]
        (is (= expected (move-back player from-space board draw-pile discard-pile)))))))

(deftest game-over-test
  (testing "Game ends when a player has all pirates on the ship"
    (let [board [{:icon :jail :pirates []}
                 {:icon :bottle :pirates [:orange]}
                 {:icon :knife :pirates []}
                 {:icon :bottle :pirates []}
                 {:icon :skull :pirates []}
                 {:icon :hat :pirates []}
                 {:icon :ship :pirates [:black :black :black :black :black :black
                                        :orange]}]]
      (is (game-over? board))))
  (testing "Game is not over if no player has all pirates on the ship"
    (let [board [{:icon :jail :pirates []}
                 {:icon :bottle :pirates [:orange]}
                 {:icon :knife :pirates []}
                 {:icon :bottle :pirates [:black]}
                 {:icon :skull :pirates []}
                 {:icon :hat :pirates []}
                 {:icon :ship :pirates [:black :black :black :black :black
                                        :orange]}]]
      (is (not (game-over? board))))))

(deftest active-player-test
  (let [game-state {:current-player "tanya"
                    :players [{:name "tanya" :color :orange :cards [:skull :knife]}
                              {:name "rusty" :color :black :cards [:key :hat]}]}
        expected {:name "tanya" :color :orange :cards [:skull :knife]}]
    (is (= expected (active-player game-state)))))

(deftest pirate-locations-for-test
  (let [board [{:icon :bottle :pirates [:orange]}
               {:icon :hat :pirates [:black]}
               {:icon :knife :pirates []}
               {:icon :bottle :pirates [:black]}
               {:icon :skull :pirates []}
               {:icon :skull :pirates [:black :orange]}
               {:icon :ship :pirates [:orange]}]
        expected [0 5 6]]
    (is (= expected (pirate-locations-for :orange board)))))

(deftest next-player-test
  (is (= "rusty" (next-player {:current-player "tanya"
                               :player-order ["tanya" "rusty"]})))
  (is (= "tanya" (next-player {:current-player "rusty"
                               :player-order ["tanya" "rusty"]}))))

(deftest update-current-player-test
  (testing "decrements moves remaining"
    (let [game-state {:current-player "tanya"
                      :actions-remaining 3
                      :player-order ["tanya" "rusty"]
                      :players [{:name "tanya" :color :orange :cards [:skull :knife]}
                                {:name "rusty" :color :black :cards [:key :hat]}]}
          expected {:current-player "tanya"
                    :actions-remaining 2
                    :player-order ["tanya" "rusty"]
                    :players [{:name "tanya" :color :orange :cards [:skull :knife]}
                              {:name "rusty" :color :black :cards [:key :hat]}]}]
      (is (= expected (update-current-player game-state)))))
  (testing "rotates player and resets move count"
    (let [game-state {:current-player "tanya"
                      :actions-remaining 1
                      :player-order ["tanya" "rusty"]
                      :players [{:name "tanya" :color :orange :cards [:skull :knife]}
                                {:name "rusty" :color :black :cards [:key :hat]}]}
          expected {:current-player "rusty"
                    :actions-remaining 3
                    :player-order ["tanya" "rusty"]
                    :players [{:name "tanya" :color :orange :cards [:skull :knife]}
                              {:name "rusty" :color :black :cards [:key :hat]}]}]
      (is (= expected (update-current-player game-state))))))