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

(defn initialize-board
  "Returns a vector populated with icons from the 6 of the board pieces concatenated."
  []
  (->> all-board-pieces
       shuffle
       (take 6)
       flatten
       vec))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
