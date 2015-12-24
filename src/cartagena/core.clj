(ns cartagena.core
  (:require [clojure.string :as str]
            [clojure.pprint :as pp])
  (:gen-class))

(def game-state (atom {}))

(def board-piece1 [:bottle :gun :hat :skull :knife :key])
(def board-piece2 [:knife :bottle :key :gun :hat :skull])
(def board-piece3 [:hat :key :gun :bottle :skull :knife])
(def board-piece4 [:key :bottle :skull :knife :hat :gun])
(def board-piece5 [:gun :key :knife :hat :skull :bottle])
(def board-piece6 [:hat :knife :key :bottle :gun :skull])
(def all-board-pieces [board-piece1 board-piece2 board-piece3 board-piece4 board-piece5 board-piece6])

(def icons [:bottle :gun :hat :key :knife :skull])

(defn initialize-board
  "Generates a board from 6 random pieces"
  []
  (let [space-icons (-> (for [piece all-board-pieces]
                          (if (zero? (rand-int 2))
                            piece
                            (reverse piece)))
                        flatten
                        (conj :jail)
                        (concat [:ship])
                        vec)]
    (vec (for [index (range 0 38)
               :let [space-icon (get space-icons index)]]
           {:index index :icon space-icon :pirates []}))))

(defn shuffle-cards
  "Shuffles and returns passed cards"
  [cards]
  (shuffle cards))

(defn initialize-cards
  "Puts the full set of cards into the discard pile"
  []
  (->> icons
       (map #(repeat 17 %))
       flatten
       shuffle-cards
       vec))

(defn initialize-player
  "Initializes a player data structure"
  [{:keys [name color]}]
  {:name name :color color :cards []})

(defn draw-cards
  "Draws cards off of the draw pile and puts them in the player's hand.  If there aren't enough cards in the draw pile, the discard pile is shuffled into the draw pile.  Returns a map of the affected player, draw pile, and discard pile."
  [n player draw-pile discard-pile]
  (if (< (count draw-pile) n)
    (let [drawn-cards draw-pile
          draw-pile (shuffle-cards discard-pile)
          left-to-draw (- n (count drawn-cards))
          more-cards (vec (take left-to-draw draw-pile))
          draw-pile (vec (drop left-to-draw draw-pile))
          all-drawn (apply conj drawn-cards more-cards)]
      {:player (assoc player :cards (apply conj (:cards player) all-drawn))
       :draw-pile draw-pile
       :discard-pile []})
    {:player (assoc player :cards (apply conj (:cards player) (take n draw-pile)))
     :draw-pile (vec (drop n draw-pile))
     :discard-pile discard-pile}))

(defn new-game!
  "Initializes a new game"
  [players]
  (let [board (initialize-board)
        players-draw-pile (loop [ps (vec (map initialize-player players))
                                 cards (initialize-cards)
                                 acc []]
                            (if (empty? ps)
                              acc
                              (let [player-draw-pile (draw-cards 6 (first ps) cards [])]
                                (recur (rest ps) (:draw-pile player-draw-pile) (conj acc player-draw-pile)))))
        init-players (vec (map :player players-draw-pile))
        draw-pile (:draw-pile (last players-draw-pile))
        pirates-in-jail (vec (flatten (map #(repeat 6 %) (map :color players))))
        board (assoc board 0 {:icon :jail :pirates pirates-in-jail})]
    (reset! game-state {:board board
                        :players init-players
                        :player-order (vec (map :name init-players))
                        :current-player (:name (first init-players))
                        :actions-remaining 3
                        :draw-pile draw-pile
                        :discard-pile []})))

(defn is-open-target?
  "Returns true if the space matches the icon and has fewer than three pirates"
  [space icon]
  (and (= icon (:icon space))
       (< (count (:pirates space)) 3)))

(defn open-space-index
  "Returns the index of the first open space for the given icon after the starting index."
  [starting-index board icon]
  (or
    (some #(let [space (get board %)]
            (when (is-open-target? space icon) %))
          (range (inc starting-index) (count board)))
    (dec (count board))))

(defn remove-pirate-from-space
  "Returns a space with the target pirate removed from the pirates collection"
  [color space]
  (let [[pre-pirates post-pirates] (split-with #(not= color %) (:pirates space))]
    (assoc space :pirates (vec (flatten (concat pre-pirates (rest post-pirates)))))))

(defn add-pirate-to-space
  "Returns a space with the target pirate added to the pirates collection"
  [color space]
  (update-in space [:pirates] conj color))

(defn play-card
  "Discards the card and moves a single pirate from the space to the next available space."
  [player icon from-space board discard-pile]
  (let [[pre-cards post-cards] (split-with #(not= icon %) (:cards player))
        updated-from-space (remove-pirate-from-space (:color player) from-space)
        space-index (.indexOf board from-space)
        next-open-space-index (open-space-index space-index board icon)
        next-open-space (get board next-open-space-index)
        updated-target-space (add-pirate-to-space (:color player) next-open-space)]
    {:player (assoc player :cards (concat pre-cards (rest post-cards)))
     :board (assoc board space-index updated-from-space
                                next-open-space-index updated-target-space)
     :discard-pile (conj discard-pile icon)}))

(defn occupied-space-index
  "Returns the index of the first space with either one or two pirates before the starting index."
  [starting-index board]
  (some #(let [space (get board %)
               pirate-count (count (:pirates space))]
          (when (or (= 1 pirate-count) (= 2 pirate-count)) %))
        (range (dec starting-index) 0 -1)))

(defn move-back
  "Moves a single pirate back to the first available space."
  [player from-space board draw-pile discard-pile]
  (when-let [prev-occupied-space-index (occupied-space-index (.indexOf board from-space) board)]
    (let [from-space-index (.indexOf board from-space)
          target-space (get board prev-occupied-space-index)
          draw-count (count (:pirates target-space))
          {:keys [player draw-pile discard-pile]} (draw-cards draw-count player draw-pile discard-pile)
          updated-from-space (remove-pirate-from-space (:color player) from-space)
          updated-target-space (add-pirate-to-space (:color player) target-space)]
      {:player player
       :board (assoc board from-space-index updated-from-space
                                  prev-occupied-space-index updated-target-space)
       :draw-pile draw-pile
       :discard-pile discard-pile})))

(defn game-over?
  "Returns true if a player has 6 pirates on the ship; otherwise, false"
  [board]
  (let [ship (first (filter #(= :ship (:icon %)) board))
        pirate-counts-by-color (frequencies (:pirates ship))]
    (some #(>= (second %) 6) pirate-counts-by-color)))

(defn end-game
  "Declares winner, etc."
  [game-state]
  (clojure.pprint/pprint game-state)
  (println (format "Congratulations %s!  You have escaped Cartagena with your pirate band!" (:current-player game-state))))

(defn next-player
  "Returns the player whose turn is... well, next"
  [game-state]
  (let [current-player-index (.indexOf (:player-order game-state) (:current-player game-state))]
    (if (= current-player-index (dec (count (:player-order game-state))))
      (get (:player-order game-state) 0)
      (get (:player-order game-state) (inc current-player-index)))))

(defn update-current-player
  "Decrements the moves remaining until the value reaches 0. Rotates the current player and resets the moves count at that point."
  [game-state]
  (let [actions-remaining (dec (:actions-remaining game-state))]
    (if (zero? actions-remaining)
      (assoc game-state :current-player (next-player game-state)
                        :actions-remaining 3)
      (assoc game-state :actions-remaining actions-remaining))))

(defn get-input
  "Waits for user to enter text and hit enter, then cleans the input"
  ([] (get-input nil))
  ([default]
   (let [input (str/trim (read-line))]
     (if (empty? input)
       default
       (str/lower-case input)))))

(defn active-player
  "Gets the active player from the players collection by name"
  [game-state]
  (first (filter #(= (:current-player game-state) (:name %)) (:players game-state))))

(defn pirate-locations-for
  "Given a color and board, returns a vector of indexes where the color appears"
  [color board]
  (vec (filter #(let [space (get board %)]
                 (when (some #{color} (:pirates space)) %))
               (range 0 (count board)))))

;; TODO: refactor to only take: active-player board discard-pile
;;  should return map of board discard-pile and updated-player
;;  called should be responsible for updating game-state
(defn prompt-play-card
  "Display card play options for current player, capture input, perform action"
  [game-state]
  (let [player (active-player game-state)
        cards (:cards player)
        pirates (pirate-locations-for (:color player) (:board game-state))]
    (println "Your cards are:" cards)
    (println "Which card? " (first cards) "is default")
    (let [card (keyword (str/replace (get-input (name (first cards))) #":" ""))]
      (println "Your pirates are on:" pirates)
      (println "Which pirate? " (first pirates) "is default")
      (let [pirate-index (read-string (get-input (str (first pirates))))
            from-space (get (:board game-state) pirate-index)
            board (:board game-state)
            discard-pile (:discard-pile game-state)
            updated-state (play-card player card from-space board discard-pile)]
        (assoc game-state :board (:board updated-state)
                          :discard-pile (:discard-pile updated-state)
                          :players (conj (remove #{player} (:players game-state)) (:player updated-state)))))))

(defn prompt-move-back
  "Display move options for current player, capture input, perform action"
  [game-state]
  (let [player (active-player game-state)
        pirates (pirate-locations-for (:color player) (:board game-state))]
    (println "Your pirates are on:" pirates)
    (println "Which pirate? " (first pirates) "is default")
    (let [pirate-index (read-string (get-input (str (first pirates))))
          from-space (get (:board game-state) pirate-index)
          board (:board game-state)
          draw-pile (:draw-pile game-state)
          discard-pile (:discard-pile game-state)
          updated-state (move-back player from-space board draw-pile discard-pile)]
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
    (if (game-over? (:board updated-game-state))
      (end-game updated-game-state)
      (prompt-action (update-current-player updated-game-state)))))

(defn display-board [game-state]
  (clojure.pprint/pprint (:board game-state)))

(defn prompt-action
  "Gets input for current player"
  [game-state]
  (display-board game-state)
  (println "active player" (active-player game-state))
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
  (new-game! [{:name "tanya" :color :orange} {:name "rusty" :color :black}])
  (prompt-action @game-state))