(ns ^:figwheel-always cartagena-web.main
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :as ajax]))

(enable-console-print!)

(def initial-game-state {:board [{:index 0,
                                  :icon :jail,
                                  :pirates [:orange :orange :orange :orange :orange :orange :black :black :black :black :black :black]}
                                 {:index 1, :icon :key, :pirates []}
                                 {:index 2, :icon :knife, :pirates []}
                                 {:index 3, :icon :skull, :pirates []}
                                 {:index 4, :icon :hat, :pirates []}
                                 {:index 5, :icon :gun, :pirates []}
                                 {:index 6, :icon :bottle, :pirates []}
                                 {:index 7, :icon :skull, :pirates []}
                                 {:index 8, :icon :hat, :pirates []}
                                 {:index 9, :icon :gun, :pirates []}
                                 {:index 10, :icon :key, :pirates []}
                                 {:index 11, :icon :bottle, :pirates []}
                                 {:index 12, :icon :knife, :pirates []}
                                 {:index 13, :icon :hat, :pirates []}
                                 {:index 14, :icon :key, :pirates []}
                                 {:index 15, :icon :gun, :pirates []}
                                 {:index 16, :icon :bottle, :pirates []}
                                 {:index 17, :icon :skull, :pirates []}
                                 {:index 18, :icon :knife, :pirates []}
                                 {:index 19, :icon :gun, :pirates []}
                                 {:index 20, :icon :hat, :pirates []}
                                 {:index 21, :icon :knife, :pirates []}
                                 {:index 22, :icon :skull, :pirates []}
                                 {:index 23, :icon :bottle, :pirates []}
                                 {:index 24, :icon :key, :pirates []}
                                 {:index 25, :icon :gun, :pirates []}
                                 {:index 26, :icon :key, :pirates []}
                                 {:index 27, :icon :knife, :pirates []}
                                 {:index 28, :icon :hat, :pirates []}
                                 {:index 29, :icon :skull, :pirates []}
                                 {:index 30, :icon :bottle, :pirates []}
                                 {:index 31, :icon :skull, :pirates []}
                                 {:index 32, :icon :gun, :pirates []}
                                 {:index 33, :icon :bottle, :pirates []}
                                 {:index 34, :icon :key, :pirates []}
                                 {:index 35, :icon :knife, :pirates []}
                                 {:index 36, :icon :hat, :pirates []}
                                 {:index 37, :icon :ship, :pirates []}],
                         :players [{:name "tanya", :color :orange, :cards [:hat :skull :knife :gun :bottle :key :gun]}
                                   {:name "rusty", :color :black, :cards [:hat :gun :knife :gun :bottle :skull]}],
                         :player-order ["tanya" "rusty"],
                         :current-player "tanya",
                         :actions-remaining 3,
                         :draw-pile [:key
                                     :bottle
                                     :gun
                                     :knife
                                     :skull
                                     :skull
                                     :bottle
                                     :key
                                     :gun
                                     :skull
                                     :gun
                                     :key
                                     :skull
                                     :key
                                     :bottle
                                     :skull
                                     :key
                                     :bottle
                                     :bottle
                                     :knife
                                     :hat
                                     :knife
                                     :knife
                                     :knife
                                     :skull
                                     :hat
                                     :skull
                                     :skull
                                     :bottle
                                     :key
                                     :gun
                                     :hat
                                     :hat
                                     :gun
                                     :gun
                                     :bottle
                                     :key
                                     :hat
                                     :hat
                                     :knife
                                     :skull
                                     :gun
                                     :key
                                     :skull
                                     :hat
                                     :bottle
                                     :bottle
                                     :skull
                                     :gun
                                     :hat
                                     :knife
                                     :key
                                     :knife
                                     :knife
                                     :gun
                                     :skull
                                     :key
                                     :hat
                                     :hat
                                     :bottle
                                     :key
                                     :knife
                                     :knife
                                     :skull
                                     :key
                                     :gun
                                     :knife
                                     :key
                                     :bottle
                                     :gun
                                     :bottle
                                     :skull
                                     :hat
                                     :key
                                     :gun
                                     :knife
                                     :hat
                                     :knife
                                     :hat
                                     :knife
                                     :bottle
                                     :key
                                     :hat
                                     :gun
                                     :bottle
                                     :hat
                                     :gun
                                     :key
                                     :bottle
                                     :skull],
                         :discard-pile []}
  )

(defonce app-state (atom initial-game-state))

(defn to-scale [n]
  (* 1.65 n))

(def icon-images {:jail "img/jail.png"
                  :ship "img/ship.png"
                  :bottle "img/bottle.png"
                  :gun "img/gun.jpg"
                  :hat "img/hat.png"
                  :key "img/key.png"
                  :knife "img/knife.png"
                  :skull "img/skull.png"})

(defn static-board []
  [;; board
   [:rect
    {:x 0
     :y 0
     :width (to-scale 500)
     :height (to-scale 300)
     :stroke "black"
     :stroke-width "0.5"
     :fill "burlywood"}]
   ;; jail
   [:rect
    {:x 0
     :y 0
     :width (to-scale 50)
     :height (to-scale 90)
     :stroke "black"
     :fill "darkgray"}]
   [:g
    {:dangerouslySetInnerHTML {:__html (str "<image xlink:href=\"img/jail.png\" x=0 y=0 width=\"" (to-scale 30) "\" height=\"" (to-scale 30) "\" />")}}]
   ;; ship
   [:rect
    {:x (to-scale 400)
     :y (to-scale 240)
     :width (to-scale 90)
     :height (to-scale 50)
     :stroke "black"
     :fill "sienna"}]
   [:g
    {:dangerouslySetInnerHTML {:__html (str "<image xlink:href=\"img/ship.png\" x=\"" (to-scale 400) "\" y=\"" (to-scale 240) "\" width=\"" (to-scale 30) "\" height=\"" (to-scale 30) "\" />")}}]
   ])

(def piece-positions
  [
   ;; jail
   {:x 0 :y 0}
   ;; row 1, left to right
   {:x 50 :y 60}
   {:x 90 :y 60}
   {:x 130 :y 60}
   {:x 170 :y 60}
   {:x 210 :y 60}
   {:x 250 :y 60}
   {:x 290 :y 60}
   {:x 330 :y 60}
   {:x 370 :y 60}
   {:x 410 :y 60}
   {:x 450 :y 60}
   ;; transition 1
   {:x 450 :y 90}
   ;; row 2, right to left
   {:x 450 :y 120}
   {:x 410 :y 120}
   {:x 370 :y 120}
   {:x 330 :y 120}
   {:x 290 :y 120}
   {:x 250 :y 120}
   {:x 210 :y 120}
   {:x 170 :y 120}
   {:x 130 :y 120}
   {:x 90 :y 120}
   {:x 50 :y 120}
   ;; transition 2
   {:x 50 :y 150}
   ;; row 3, left to right
   {:x 50 :y 180}
   {:x 90 :y 180}
   {:x 130 :y 180}
   {:x 170 :y 180}
   {:x 210 :y 180}
   {:x 250 :y 180}
   {:x 290 :y 180}
   {:x 330 :y 180}
   {:x 370 :y 180}
   {:x 410 :y 180}
   {:x 450 :y 180}
   ;; transition 3
   {:x 450 :y 210}
   ;; ship
   {:x 400 :y 240}
 ])

(defn pirate-click [game-state color from-space-index]
  (println "clicked pirate:" color from-space-index))

(defn jail []
  (when-let [jail (get-in @app-state [:board 0])]
    (apply concat
           (let [pirate-frequencies (frequencies (:pirates jail))
                 pirate-colors (vec (keys pirate-frequencies))]
             (for [player-index (range (count pirate-frequencies))]
               (let [pirate-color (get pirate-colors player-index)
                     pirate-count (pirate-color pirate-frequencies)
                     color-name (name pirate-color)
                     x (to-scale (+ 5 (* 10 player-index)))]
                 (for [pirate-index (range pirate-count)]
                   (let [y (to-scale (+ 35 (* 10 pirate-index)))]
                     [:circle
                      {:cx x
                       :cy y
                       :r (to-scale 4)
                       :fill color-name
                       :on-click (fn jail-click [e]
                                   (pirate-click @app-state pirate-color 0))}]))))))))

;; TODO: this looks almost exactly like jail
(defn ship []
  (when-let [ship (get-in @app-state [:board 37])]
    (apply concat
           (let [pirate-frequencies (frequencies (:pirates ship))
                 pirate-colors (vec (keys pirate-frequencies))]
             (for [player-index (range (count pirate-frequencies))]
               (let [pirate-color (get pirate-colors player-index)
                     pirate-count (pirate-color pirate-frequencies)
                     color-name (name pirate-color)
                     x (to-scale (+ 445 (* 10 player-index)))]
                 (for [pirate-index (range pirate-count)]
                   (let [y (to-scale (+ 245 (* 10 pirate-index)))]
                     [:circle
                      {:cx x
                       :cy y
                       :r (to-scale 4)
                       :fill color-name
                       :on-click (fn ship-click [e]
                                   (pirate-click @app-state pirate-color 37))}]))))))))

(defn normal-space [x y]
  [:rect
   {:x (to-scale x)
    :y (to-scale y)
    :width (to-scale 40)
    :height (to-scale 30)
    :stroke "black"
    :stroke-width "0.5"
    :fill "lightgray"}])

(defn space-image [x y icon]
  [:g
   {:dangerouslySetInnerHTML
    {:__html (str "<image xlink:href=\"" (icon icon-images) "\" x=\"" (to-scale x) "\" y=\"" (to-scale y) "\" width=\"" (to-scale 30) "\" height=\"" (to-scale 30) "\" />")}}])

(defn normal-spaces []
  (apply concat
         (for [i (range 1 37)]
           (when-let [space-data (get-in @app-state [:board i])]
             (let [position (get piece-positions i)
                   space (normal-space (:x position) (:y position))
                   image (space-image (:x position) (:y position) (:icon space-data))]
               [space image]
               )))))

;; TODO: duplicated from server code
(defn active-player
  "Returns the active player from the players collection by name."
  [game-state]
  (first (filter #(= (:current-player game-state) (:name %)) (:players game-state))))

(defn card-click [game-state card]
  (println "card-click" card))

(defn main-view []
  [:center
   [:h1 "CARTAGENA"
    [:div
     (-> [:svg
          {:view-box (str "0 0 " (to-scale 501) " " (to-scale 301))
           :width (to-scale 501)
           :height (to-scale 301)}]
         (into (static-board))
         (into (jail))
         (into (normal-spaces))
         (into (ship))
         )]
    (let [{:keys [color cards] player-name :name} (active-player @app-state)
          card-groups (frequencies cards)]
      [:div
       [:table
        [:tr
         [:td "Player"]
         [:td player-name]]
        [:tr
         [:td "Color"]
         [:td
          [:svg
           {:width (to-scale 20)
            :height (to-scale 20)}
           [:circle
            {:cx (to-scale 10)
             :cy (to-scale 10)
             :r (to-scale 7)
             :fill (name color)}]]
          [:span {:style {:color (name color)}} (name color)]]]
        [:tr
         [:td "Cards"]
         [:td (for [[card num] card-groups]
                ^{:key card}
                [:span {:style {:float "left"}}
                 [:figure
                  [:img
                   {:src (card icon-images)
                    :width (to-scale 30)
                    :height (to-scale 30)
                    :on-click (fn ship-click [e]
                                   (card-click @app-state card))}]
                  [:center [:figcaption num]]]])]]]])]])

(defn on-error [{:keys [status status-text]}]
  (.log js/console (str "ERROR [" status "] " status-text)))

(defn on-new-game [response]
  (reset! app-state response))

(defn new-game! []
  (ajax/GET "http://localhost:3000/fake-game"
            {:handler on-new-game
             :error-handler on-error}))

(defn ^:export main []
  (when-let [app (. js/document (getElementById "app"))]
    (reagent/render-component [main-view] app)))

(main)

(defn on-js-reload []
  (main))
