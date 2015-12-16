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
  (->> all-board-pieces
       shuffle
       (take 6)
       flatten
       vec))

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
  "Pulls cards off the top of the draw pile, returning a map of the new hand and what remains in the draw pile"
  [n player draw-pile]
  {:player (assoc player :cards (cond (:cards player) (vec (take n draw-pile))))
   :draw-pile (vec (drop n draw-pile))})

(defn new-game!
  "Initializes a new game"
  [players]
  (let [board (initialize-board)
        players-draw-pile (loop [ps (vec (map initialize-player players))
                                 cards (initialize-cards)
                                 acc []]
                            (if (empty? ps)
                              acc
                              (let [player-draw-pile (draw-cards 6 (first ps) cards)]
                                (recur (rest ps) (:draw-pile player-draw-pile) (conj acc player-draw-pile)))))
        init-players (vec (map :player players-draw-pile))
        draw-pile (:draw-pile (last players-draw-pile))]
    (reset! game-state {:board-spaces board
                        :players init-players
                        :current-player 0
                        :draw-pile draw-pile
                        :discard-pile []}))


  #_(let [game-state (assoc {}
                     :players
                     (vec (for [player players]
                            (initialize-player player))))]
    (assoc game-state :current-player 0)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
