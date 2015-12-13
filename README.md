# cartagena

An engine for the Cartagena board game.

## Installation

I'll let you know once it's moved beyond test code.

## Usage

Clojure makes java jars, and they are not filled with Hyrulean rupees.

    $ java -jar cartagena-0.1.0-standalone.jar [args]

## Examples

...

## Bugs

...

## Rules

### Board pieces

The Cartagena board is made up of 6 pieces with 6 spaces each.  Each space has an icon on it representing which card must be played to advance to it.  The icons include:

* bottle
* gun
* hat
* key
* knife
* skull

The board pieces are printed on both sides.  The pieces are configured as follows:

* bottle gun hat skull knife key
* knife bottle key gun hat skull
* hat key gun bottle skull knife
* key bottle skull knife hat gun
* gun key knife hat skull bottle
* hat knife key bottle gun skull

The pieces can be aligned in either direction.

### Cards

There are 102 cards: 17 of each of the above-listed icons.  Another version of the game has 103 cards -- one card with an arrow on it.  We will not be covering that version in this implementation.

### Pieces

There are 30 pirates: 6 of each of 5 colors -- red, green, yellow, blue, brown. The colors are, in fact, arbitrary, as long as there are 6 pieces of 5 distinct colors.  Given that I like orange more than yellow, we'll be substituting.

### Setup

* Between two and five players choose to play.
* Players choose their color.
* Randomly align the six board pieces.
* All player pieces are put in the jail at one end of the board.  The pirate ship is placed at the other end of the board.
* Shuffle the cards and deal 6 to each player, face down.  The rest of the cards are placed in the middle to draw from.
* Decide who goes first.  The decision can be made in any way the players agree on.  I suggest that the most pirate-y person go first.  Play then progresses clockwise.

### How to play

On your turn, you may make from 1 to 3 moves.  There are two types of moves.

* _Advance a single pirate by playing a card from your hand._  Choose the card from your hand and put it in the discard pile.  Select a single pirate and advance that pirate to the first unoccupied space marked with the same icon represented by the card you just discarded.  If there is no unoccupied space with the corresponding icon ahead of your pirate, you may advance the pirate onto the ship.
* _Move a single pirate backward to the first space occupied by only one or two pirates._  The space can be occupied by either your or other players' pirates.  Vacant spaces and spaces occupied by three pirates are skipped.  If you land on a space with a single pirate, draw one card; landing on a space with two pirates means you draw two cards.
 
### Winning

The game ends when one player has moved all of his/her pirates onto the ship.  They become captain of the vessel, and escape from the island to sail the seven seas with their swarthy pirate crew...

...at least, they do until they're captured again!  :-)

## License

Copyright © 2015 rusty-software

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
