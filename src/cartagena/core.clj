(ns cartagena.core
  (:gen-class))

(def card1 [:bottle :gun :hat :skull :knife :key])
(def card2 [:knife :bottle :key :gun :hat :skull])
(def card3 [:hat :key :gun :bottle :skull :knife])
(def card4 [:key :bottle :skull :knife :hat :gun])
(def card5 [:gun :key :knife :hat :skull :bottle])
(def card6 [:hat :knife :key :bottle :gun :skull])
(def card1r (vec (reverse card1)))
(def card2r (vec (reverse card2)))
(def card3r (vec (reverse card3)))
(def card4r (vec (reverse card4)))
(def card5r (vec (reverse card5)))
(def card6r (vec (reverse card6)))
(def all-cards [card1 card2 card3 card4 card5 card6
                card1r card2r card3r card4r card5r card6r])

(defn initialize-board
  "Returns a vector populated with icons from the 6 of the board pieces concatenated."
  []
  (->> all-cards
       shuffle
       (take 6)
       flatten
       vec))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
