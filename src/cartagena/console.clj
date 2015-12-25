(ns cartagena.console
  (:require [clojure.string :as str]
            [cartagena.core :as engine]))

(defn end-game
  "Declares winner, etc."
  [game-state]
  (clojure.pprint/pprint game-state)
  (println (format "Congratulations %s!  You have escaped Cartagena with your pirate band!" (:current-player game-state))))

(defn get-input
  "Waits for user to enter text and hit enter, then cleans the input"
  ([] (get-input nil))
  ([default]
   (let [input (str/trim (read-line))]
     (if (empty? input)
       default
       (str/lower-case input)))))

(defn prompt-play-card
  "Display card play options for current player, capture input, perform action"
  [game-state]
  (let [player (engine/active-player game-state)
        cards (:cards player)
        pirates (engine/pirate-locations-for (:color player) (:board game-state))]
    (println "Your cards are:" cards)
    (println "Which card? " (first cards) "is default")
    (let [card (keyword (str/replace (get-input (name (first cards))) #":" ""))]
      (println "Your pirates are on:" pirates)
      (println "Which pirate? " (first pirates) "is default")
      (let [pirate-index (read-string (get-input (str (first pirates))))
            from-space (get (:board game-state) pirate-index)
            board (:board game-state)
            discard-pile (:discard-pile game-state)
            updated-state (engine/play-card player card from-space board discard-pile)]
        (assoc game-state :board (:board updated-state)
                          :discard-pile (:discard-pile updated-state)
                          :players (conj (remove #{player} (:players game-state)) (:player updated-state)))))))

(defn prompt-move-back
  "Display move options for current player, capture input, perform action"
  [game-state]
  (let [player (engine/active-player game-state)
        pirates (engine/pirate-locations-for (:color player) (:board game-state))]
    (println "Your pirates are on:" pirates)
    (println "Which pirate? " (first pirates) "is default")
    (let [pirate-index (read-string (get-input (str (first pirates))))
          from-space (get (:board game-state) pirate-index)
          board (:board game-state)
          draw-pile (:draw-pile game-state)
          discard-pile (:discard-pile game-state)
          updated-state (engine/move-back player from-space board draw-pile discard-pile)]
      (assoc game-state :board (:board updated-state)
                        :draw-pile (:draw-pile updated-state)
                        :discard-pile (:discard-pile updated-state)
                        :players (conj (remove #{player} (:players game-state)) (:player updated-state))))))

(defn no-action
  "Takes no active action; returns the game state as-is"
  [game-state]
  game-state)
(declare prompt-action)

(defn perform-action
  "Acts out the selected action by gathering more input and calling appropriate functions"
  [action-fn game-state]
  (let [updated-game-state (action-fn game-state)]
    (if (engine/game-over? (:board updated-game-state))
      (end-game updated-game-state)
      (let [updated-player-state (engine/update-current-player (:actions-remaining updated-game-state)
                                                               (:current-player updated-game-state)
                                                               (:player-order updated-game-state))]
        (prompt-action (assoc updated-game-state :actions-remaining (:actions-remaining updated-player-state)
                                                 :current-player (:current-player updated-player-state)))))))

(defn display-board [game-state]
  (clojure.pprint/pprint (:board game-state)))

(defn prompt-action
  "Gets input for current player"
  [game-state]
  (display-board game-state)
  (println)
  (println "active player" (engine/active-player game-state))
  (println "You have" (:actions-remaining game-state) "actions left!  Choose! ([1] is default)")
  (println "[1] Play card")
  (println "[2] Move back")
  (println "[3] Pass")
  (let [current-action (get-input "1")]
    (case current-action
      "1" (perform-action prompt-play-card game-state)
      "2" (perform-action prompt-move-back game-state)
      (perform-action no-action game-state))))

(defn -main
  "Calls the function to get the number of players... goes on from there"
  [& args]
  (prompt-action (engine/new-game! [{:name "tanya" :color :orange} {:name "rusty" :color :black}])))
