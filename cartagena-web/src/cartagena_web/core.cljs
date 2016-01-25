(ns ^:figwheel-always cartagena-web.core
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :as ajax]))

(enable-console-print!)

(defonce app-state (atom nil))

(defn to-scale [n]
  (* 1.5 n))

(defn static-board []
  [;; board
   [:rect {:x 0 :y 0 :width (to-scale 500) :height (to-scale 300) :stroke "black" :stroke-width "0.5" :fill "burlywood"}]
   ;; jail
   [:rect {:x 0 :y 0 :width (to-scale 50) :height (to-scale 90) :stroke "black" :fill "darkgray"}]
   [:text {:x 0 :y (to-scale 15) :style {:text-anchor "start" :stroke "none" :fill "black" :font-size "smaller"}} "jail"]
   ;; ship
   [:rect {:x (to-scale 400) :y (to-scale 240) :width (to-scale 90) :height (to-scale 50) :stroke "black" :fill "sienna"}]
   [:text {:x (to-scale 400) :y (to-scale 255) :style {:text-anchor "start" :stroke "none" :fill "black" :font-size "smaller"}} "ship"]
   ])



(def piece-positions
  [
   ;; jail
   {:x 0 :y 0 :text-x 0 :text-y 15}
   ;; row 1, left to right
   {:x 50 :y 60 :text-x 50 :text-y 75}
   {:x 90 :y 60 :text-x 90 :text-y 75}
   {:x 130 :y 60 :text-x 130 :text-y 75}
   {:x 170 :y 60 :text-x 170 :text-y 75}
   {:x 210 :y 60 :text-x 210 :text-y 75}
   {:x 250 :y 60 :text-x 250 :text-y 75}
   {:x 290 :y 60 :text-x 290 :text-y 75}
   {:x 330 :y 60 :text-x 330 :text-y 75}
   {:x 370 :y 60 :text-x 370 :text-y 75}
   {:x 410 :y 60 :text-x 410 :text-y 75}
   {:x 450 :y 60 :text-x 450 :text-y 75}
   ;; transition 1
   {:x 450 :y 90 :text-x 450 :text-y 105}
   ;; row 2, right to left
   {:x 450 :y 120 :text-x 450 :text-y 135}
   {:x 410 :y 120 :text-x 410 :text-y 135}
   {:x 370 :y 120 :text-x 370 :text-y 135}
   {:x 330 :y 120 :text-x 330 :text-y 135}
   {:x 290 :y 120 :text-x 290 :text-y 135}
   {:x 250 :y 120 :text-x 250 :text-y 135}
   {:x 210 :y 120 :text-x 210 :text-y 135}
   {:x 170 :y 120 :text-x 170 :text-y 135}
   {:x 130 :y 120 :text-x 130 :text-y 135}
   {:x 90 :y 120 :text-x 90 :text-y 135}
   {:x 50 :y 120 :text-x 50 :text-y 135}
   ;; transition 2
   {:x 50 :y 150 :text-x 50 :text-y 165}
   ;; row 3, left to right
   {:x 50 :y 180 :text-x 50 :text-y 195}
   {:x 90 :y 180 :text-x 90 :text-y 195}
   {:x 130 :y 180 :text-x 130 :text-y 195}
   {:x 170 :y 180 :text-x 170 :text-y 195}
   {:x 210 :y 180 :text-x 210 :text-y 195}
   {:x 250 :y 180 :text-x 250 :text-y 195}
   {:x 290 :y 180 :text-x 290 :text-y 195}
   {:x 330 :y 180 :text-x 330 :text-y 195}
   {:x 370 :y 180 :text-x 370 :text-y 195}
   {:x 410 :y 180 :text-x 410 :text-y 195}
   {:x 450 :y 180 :text-x 450 :text-y 195}
   ;; transition 3
   {:x 450 :y 210 :text-x 450 :text-y 225}
   ;; ship
   {:x 400 :y 240 :text-x 400 :text-y 415}
 ])

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
                     [:circle {:cx x :cy y :r (to-scale 4) :fill color-name}]))))))))

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
                     [:circle {:cx x :cy y :r (to-scale 4) :fill color-name}]))))))))

(defn normal-space [x y]
  [:rect {:x (to-scale x) :y (to-scale y) :width (to-scale 40) :height (to-scale 30) :stroke "black" :stroke-width "0.5" :fill "lightgray"}])

(defn space-text [x y s]
  [:text {:x (to-scale x) :y (to-scale y) :style {:text-anchor "start" :stroke "none" :fill "black" :font-size "small"}} s])

(defn normal-spaces []
  (apply concat
         (for [i (range 1 37)]
           (when-let [space-data (get-in @app-state [:board i])]
             (let [position (get piece-positions i)
                   space (normal-space (:x position) (:y position))
                   text (space-text (:text-x position) (:text-y position) (name (:icon space-data)))]
               [space text]
               )))))

(defn main-view []
  [:center
   [:h1 "CARTAGENA"
    [:div
     [:div
      (-> [:svg
           {:view-box (str "0 0" (to-scale 501) (to-scale 301))
            :width (to-scale 501)
            :height (to-scale 301)}]
          (into (static-board))
          (into (jail))
          (into (normal-spaces))
          (into (ship))
          )]
     [:div
      [:label "player area"]]]
    ]])

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
    (new-game!)
    (reagent/render-component [main-view] app)))

(main)

(defn on-js-reload []
  (main))
