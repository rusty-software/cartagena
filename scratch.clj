{
 ;; board is a collection of 36 entries, each representing a single space.
 ;; They are sequential and should not be shuffled once the initial state is set.
 ;; Should they contain a position marker as well?
 ;; ACTION: add Jail and Ship to beginning and end of board
 :board [
         {:icon :hat :pirates []}
         {:icon :bottle :pirates [:orange]}
         {:icon :key :pirates [:orange :black]}
         {:icon :skull :pirates [:orange :orange :orange]}
         ;; ... to 36 entries, indexed 0 to 35
         ]

 ;; vector of all of the cards that are available to draw.  Begins with 102
 ;; cards (17 of each icon) before any are drawn by the players.  Order matters
 ;; here, as the players draw from the top of the pile.
 :draw-pile [:hat :key :bottle :key :gun :skull :skull :knife :hat :bottle :bottle]

 ;; vector of all the cards that have been played.  When the draw pile is
 ;; depleted, the shuffled contents of this vector are assigned to the draw
 ;; pile, then this pile is reset to empty.
 :discard-pile [:bottle :hat :skull :gun :key]

 ;; players is a collection of up to 6 players
 ;; notes on player map:
 ;;   pirate;
 ;;     vector containing index positions on the board
 ;;     two magic values exist: 36 for being on the ship, -1 for being in jail
 ;;     no more than 3 pirates can share a specific index
 ;;     might be easier if the board spaces had a position as well instead of
 ;;     relying on the index...
 ;;   cards;
 ;;     vector of keywords for cards
 ;;     correspond to icons on the board
 ;; ACTION: dump pirates from player since the board manages them
 :players [
           {:name "tanya"
            :color :orange
            :pirates [36 24 21 3 3 -1]
            :cards [:skull :key :key :bottle :hat :knife]}
           {:name "rusty"
            :color :black
            :pirates [24 18 8 3 -1 -1]
            :cards [:gun :bottle :skull :skull]}
           ;; ... up to 6, depending on how many playeres were initialized
           ]

 ;; cardinal order in which players take turns.  Here, Tanya is first, then
 ;; Rusty
 :player-order ["tanya" "rusty"]

 ;; name of the current player.  When the curren turn ends, player-order is used
 ;; to determine who the next player should be
 :current-player "tanya"
 }
