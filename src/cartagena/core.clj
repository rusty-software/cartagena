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

(def draw-pile (atom []))
(def discard-pile (atom []))
(defn place-cards!
  "Puts the full set of cards into the discard pile"
  []
  (reset! discard-pile (->> icons
                            (map #(repeat 17 %))
                            flatten
                            vec)))

(defn shuffle-cards!
  "Shuffles the card in the discard pile, placing them in the draw pile"
  []
  (when (and (not (empty? @discard-pile))
             (empty? @draw-pile))
    (reset! draw-pile (vec (shuffle @discard-pile)))
    (reset! discard-pile [])))

(defn initialize-board!
  "Returns a vector populated with icons from the 6 of the board pieces concatenated."
  []
  (->> all-board-pieces
       shuffle
       (take 6)
       flatten
       vec))

(defn draw-cards!
  "Takes n cards off of the top of the draw pile"
  [n]
  (let [cards (take n @draw-pile)]
    (reset! draw-pile (drop n @draw-pile))
    (vec cards)))

(defn initialize-player
  "Initializes a player data structure"
  [{:keys [name color]}]
  {:name name :color color :pirates [-1 -1 -1 -1 -1 -1] :cards (draw-cards! 6)})

(defn new-game
  "Initializes a new game"
  [players]
  (place-cards!)
  (shuffle-cards!)
  (let [game-state (assoc {}
                     :players
                     (vec (for [player players]
                            (initialize-player player))))]
    (assoc game-state :current-player 0)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
