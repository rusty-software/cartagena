(ns cartagena.core
  (:gen-class))

(def board-piece1 [:bottle :gun :hat :skull :knife :key])
(def board-piece2 [:knife :bottle :key :gun :hat :skull])
(def board-piece3 [:hat :key :gun :bottle :skull :knife])
(def board-piece4 [:key :bottle :skull :knife :hat :gun])
(def board-piece5 [:gun :key :knife :hat :skull :bottle])
(def board-piece6 [:hat :knife :key :bottle :gun :skull])
(def board-piece1r (vec (reverse board-piece1)))
(def board-piece2r (vec (reverse board-piece2)))
(def board-piece3r (vec (reverse board-piece3)))
(def board-piece4r (vec (reverse board-piece4)))
(def board-piece5r (vec (reverse board-piece5)))
(def board-piece6r (vec (reverse board-piece6)))
(def all-board-pieces [board-piece1 board-piece2 board-piece3 board-piece4 board-piece5 board-piece6
                       board-piece1r board-piece2r board-piece3r board-piece4r board-piece5r board-piece6r])

(def icons [:bottle :gun :hat :key :knife :skull])

(def game-state (atom {}))

(defn initialize-board
  "Generates a board from 6 random pieces"
  []
  (let [space-icons (->> all-board-pieces
                         shuffle
                         (take 6)
                         flatten)]
    (vec (for [space-icon space-icons]
           {:icon space-icon :pirates []}))))

(defn shuffle-cards
  "Shuffles and returns passed cards"
  [cards]
  (vec (shuffle cards)))

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
  {:name name :color color :pirates [-1 -1 -1 -1 -1 -1] :cards []})

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
        draw-pile (:draw-pile (last players-draw-pile))]
    (reset! game-state {:board-spaces board
                        :players init-players
                        :player-order (vec (map :name init-players))
                        :current-player (:name (first init-players))
                        :draw-pile draw-pile
                        :discard-pile []})))

(defn update-player!
  "Updates the data for a single player by name"
  [name kvs]
  (let [player (first (filter #(= name (:name %)) (:players @game-state)))
        updated-player (merge player kvs)
        updated-players (merge (remove #(= name (:name %)) (:players @game-state)) updated-player)
        updated-state (assoc @game-state :players updated-players)]
    (reset! game-state updated-state)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))