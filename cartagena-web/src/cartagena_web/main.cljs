(ns ^:figwheel-always cartagena-web.main
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :as ajax]))

(enable-console-print!)

(defonce app-state (atom nil))
(def empty-names {:black nil :blue nil :green nil :orange nil :red nil})
(defonce names (atom empty-names))

(defn set-name! [color name]
  (swap! names assoc color name))

(defn get-name [color]
  (color @names))

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
    {:x (to-scale 410)
     :y (to-scale 240)
     :width (to-scale 80)
     :height (to-scale 60)
     :stroke "black"
     :fill "sienna"}]
   [:g
    {:dangerouslySetInnerHTML {:__html (str "<image xlink:href=\"img/ship.png\" x=\"" (to-scale 410) "\" y=\"" (to-scale 240) "\" width=\"" (to-scale 30) "\" height=\"" (to-scale 30) "\" />")}}]
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
   {:x 400 :y 240}])

;; TODO: duplicated from server code
(defn active-player
  "Returns the active player from the players collection by name."
  [game-state]
  (first (filter #(= (:current-player game-state) (:name %)) (:players game-state))))

(defn on-error [{:keys [status status-text]}]
  (.log js/console (str "ERROR [" status "] " status-text)))

(defn on-new-game [response]
  (reset! app-state response))

(defn new-game! []
  (let [players (cond-> []
                        (get-name :black) (conj {:name (get-name :black) :color :black})
                        (get-name :blue) (conj {:name (get-name :blue) :color :blue})
                        (get-name :green) (conj {:name (get-name :green) :color :green})
                        (get-name :orange) (conj {:name (get-name :orange) :color :orange})
                        (get-name :red) (conj {:name (get-name :red) :color :red}))]
  (reset! app-state (dissoc @app-state :select-players))
  (swap! names empty-names)
  (ajax/POST "http://localhost:3000/new-game"
             {:params {:players (shuffle players)}
              :handler on-new-game
              :error-handler on-error})))

(defn select-players! []
  (reset! app-state (assoc @app-state :select-players true)))

(defn on-update-active-player [response]
  (reset! app-state (assoc @app-state :actions-remaining (:actions-remaining response)
                                     :current-player (:current-player response))))

(defn update-active-player! [{:keys [actions-remaining current-player player-order]}]
  (ajax/POST "http://localhost:3000/update-active-player"
             {:params {:actions-remaining actions-remaining
                       :current-player current-player
                       :player-order player-order}
              :handler on-update-active-player
              :error on-error}))

;; TODO: figure out a way to make this work elegantly with callbacks
;; swiped from server code
(defn game-over?
  "Returns truthy if a player has 6 pirates on the ship; otherwise nil."
  [board]
  (let [ship (first (filter #(= :ship (:icon %)) board))
        pirate-counts-by-color (frequencies (:pirates ship))]
    (some #(>= (second %) 6) pirate-counts-by-color)))

(defn end-game! []
  (reset! app-state (assoc @app-state :game-over true)))

(defn on-play-card [response]
  (let [board (:board response)]
    (reset! app-state (assoc @app-state :board (:board response)
                                        :discard-pile (:discard-pile response)
                                        :players (conj (remove #{(active-player @app-state)} (:players @app-state)) (:player response))))
    (reset! app-state (dissoc @app-state :selected-card))
    (if (game-over? board)
      (end-game!)
      (update-active-player! @app-state))))

(defn play-card! [player card from-space board discard-pile]
  (ajax/POST "http://localhost:3000/play-card"
             {:params {:player player
                       :icon card
                       :from-space from-space
                       :board board
                       :discard-pile discard-pile}
              :handler on-play-card
              :error-handler on-error}))

(defn on-move-back [response]
  (when-let [board (:board response)]
    (reset! app-state (assoc @app-state :board board
                                        :draw-pile (:draw-pile response)
                                        :discard-pile (:discard-pile response)
                                        :players (conj (remove #{(active-player @app-state)} (:players @app-state)) (:player response))))
    (update-active-player! @app-state)))

(defn move-back! [player from-space board draw-pile discard-pile]
  (ajax/POST "http://localhost:3000/move-back"
             {:params {:player player
                       :from-space from-space
                       :board board
                       :draw-pile draw-pile
                       :discard-pile discard-pile}
              :handler on-move-back
              :error-handler on-error}))

(defn pirate-click [color from-space-index]
  (when (= color (:color (active-player @app-state)))
    (let [player (active-player @app-state)
          board (:board @app-state)
          from-space (get board from-space-index)
          discard-pile (:discard-pile @app-state)]
      (if-let [selected-card (:selected-card @app-state)]
        (play-card! player selected-card from-space board discard-pile)
        (move-back! player from-space board (:draw-pile @app-state) discard-pile)))))

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
                                   (pirate-click pirate-color 0))}]))))))))

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
                                   (pirate-click pirate-color 37))}]))))))))

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

(defn circles-for [space-index x y colors]
  (for [color-index (range (count colors))]
    (let [color (get colors color-index)
          color-name (name color)
          cx (to-scale (+ 35 x))
          cy (to-scale (+ y 5 (* 10 color-index)))]
      ^{:key color-index}
      [:circle
       {:cx cx
        :cy cy
        :r (to-scale 4)
        :fill color-name
        :on-click (fn circle-click [e]
                    (pirate-click color space-index))}])))

(defn normal-spaces []
  (apply concat
         (for [i (range 1 37)]
           (when-let [space-data (get-in @app-state [:board i])]
             (let [position (get piece-positions i)
                   x (:x position)
                   y (:y position)
                   space (normal-space x y)
                   image (space-image x y (:icon space-data))
                   pirates (circles-for i x y (:pirates space-data))]
               (conj [space image] pirates))))))

(defn select-card! [card]
  (reset! app-state (assoc @app-state :selected-card card)))

(defn unselect-card! []
  (reset! app-state (dissoc @app-state :selected-card)))

(defn main-view []
  [:center
   [:h1 "CARTAGENA"]
   [:div
    [:button
     {:class "btn btn-primary"
      :on-click (fn button-click [e]
                  (select-players!))}
     "New Game"]]
   (when (and @app-state (:game-over @app-state))
     [:div
      {:class "row"}
      [:h2 "WE HAVE A WINNER!"]
      [:h3 (str "Congratulations, " (:name (active-player @app-state)) "!")]])
   (when (and @app-state (:select-players @app-state))
     [:div
      {:class "row"}
      [:div
       {:class "col-md-4 col-md-offset-4"}
       [:table {:class "table table-bordered"}
        [:thead
         [:tr
          [:th "Color"]
          [:th "Player"]]]
        [:tbody
         [:tr
          [:td
           [:label {:for "black-name" :style {:color "black"}} "black"]]
          [:td
           [:input
            {:type "text"
             :name "black-name"
             :value (get-name :black)
             :on-change #(set-name! :black (-> % .-target .-value))}]]]
         [:tr
          [:td
           [:label {:for "blue-name" :style {:color "blue"}} "blue"]]
          [:td
           [:input
            {:type "text"
             :name "blue-name"
             :value (get-name :blue)
             :on-change #(set-name! :blue (-> % .-target .-value))}]]]
         [:tr
          [:td
           [:label {:for "green-name" :style {:color "green"}} "green"]]
          [:td
           [:input
            {:type "text"
             :name "green-name"
             :value (get-name :green)
             :on-change #(set-name! :green (-> % .-target .-value))}]]]
         [:tr
          [:td
           [:label {:for "orange-name" :style {:color "orange"}} "orange"]]
          [:td
           [:input
            {:type "text"
             :name "orange-name"
             :value (get-name :orange)
             :on-change #(set-name! :orange (-> % .-target .-value))}]]]
         [:tr
          [:td
           [:label {:for "red-name" :style {:color "red"}} "red"]]
          [:td
           [:input
            {:type "text"
             :name "red-name"
             :value (get-name :red)
             :on-change #(set-name! :red (-> % .-target .-value))}]]]
         [:tr
          [:td {:colSpan 2}
           [:center
            [:button {:class "btn btn-success"
                      :on-click (fn btn-click [e]
                                  (new-game!))}
             "Start"]]]]]]]])
   [:div
    {:class "row"
     :style {:float "left" :margin-left 10 :margin-right 10}}
    [:div
     {:class "col-md-5"}
     (-> [:svg
          {:view-box (str "0 0 " (to-scale 501) " " (to-scale 301))
           :width (to-scale 501)
           :height (to-scale 301)}]
         (into (static-board))
         (into (normal-spaces))
         (into (jail))
         (into (ship))
         )]]
   (when-let [active-player (active-player @app-state)]
     (let [{:keys [color cards] player-name :name} active-player
           card-groups (frequencies cards)]
       [:div
        {:class "col-md-4"}
        [:div {:style {:margin-right 10}}
         [:table {:class "table table-bordered table-responsive"}
          [:tbody
           [:tr
            [:td "Current Player"]
            [:td player-name]]]
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
           [:td "Actions Remaining"]
           [:td (:actions-remaining @app-state)]]
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
                      :on-click (fn img-click [e]
                                  (select-card! card))}]
                    [:center [:figcaption num]]]])]]
          [:tr
           [:td "Selected Card"]
           [:td [:span {:style {:float "left"}}
                 (when-let [selected-card (:selected-card @app-state)]
                   [:img
                    {:src (selected-card icon-images)
                     :width (to-scale 30)
                     :height (to-scale 30)
                     :on-click (fn img-click [e]
                                 (unselect-card!))}])]]]
          [:tr
           [:td {:colSpan 2}
            [:center
             [:button
              {:class "btn"
               :on-click (fn btn-click [e]
                                   (update-active-player! @app-state))} "Pass"]]]]]]
        [:div
         [:p "To move forward, click a card, then click the target pirate.  To undo card selection, click the selected card."]
         [:p "To move backward, click the target pirate."]]]))])

(defn ^:export main []
  (when-let [app (. js/document (getElementById "app"))]
    (reagent/render-component [main-view] app)))

(main)

(defn on-js-reload []
  (main))
