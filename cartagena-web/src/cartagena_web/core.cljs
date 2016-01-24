(ns ^:figwheel-always cartagena-web.core
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :as ajax]))

(enable-console-print!)

(defonce app-state (atom nil))

(defn to-scale [n]
  (* 1.5 n))

(defn static-board []
  [;; playmat
   [:rect {:x 0 :y 0 :width 1000 :height 750 :stroke "black" :stroke-width "1" :fill "tan"}]
   ;; board
   [:rect {:x 0 :y 0 :width (to-scale 500) :height (to-scale 400) :stroke "black" :stroke-width "0.5" :fill "burlywood"}]
   ;; jail
   [:rect {:x 0 :y 0 :width (to-scale 50) :height (to-scale 90) :stroke "black" :fill "darkgray"}]
   [:text {:x 0 :y (to-scale 15) :style {:text-anchor "start" :stroke "none" :fill "black" :font-size "smaller"}} "jail"]
   ;; ship
   [:rect {:x (to-scale 410) :y (to-scale 250) :width (to-scale 90) :height (to-scale 50) :stroke "black" :fill "sienna"}]
   [:text {:x (to-scale 410) :y (to-scale 260) :style {:text-anchor "start" :stroke "none" :fill "black" :font-size "smaller"}} "ship"]

   [:circle {:cx (to-scale 455) :cy (to-scale 255) :r (to-scale 4) :fill "chartreuse"}]
   [:circle {:cx (to-scale 455) :cy (to-scale 295) :r (to-scale 4) :fill "chartreuse"}]
   [:circle {:cx (to-scale 465) :cy (to-scale 255) :r (to-scale 4) :fill "cyan"}]
   [:circle {:cx (to-scale 475) :cy (to-scale 255) :r (to-scale 4) :fill "yellow"}]
   [:circle {:cx (to-scale 485) :cy (to-scale 255) :r (to-scale 4) :fill "fuchsia"}]
   [:circle {:cx (to-scale 495) :cy (to-scale 255) :r (to-scale 4) :fill "red"}]
   ])

(def good-example (into [:svg {:w 10 :h 8}]
                        [[:circle {:x 1 :y 2}] [:circle {:x 2 :y 4}]]))

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
   ])

(defn normal-space [x y]
  [:rect {:x (to-scale x) :y (to-scale y) :width (to-scale 40) :height (to-scale 30) :stroke "black" :stroke-width "0.5" :fill "lightgray"}])

(defn space-text [x y s]
  [:text {:x (to-scale x) :y (to-scale y) :style {:text-anchor "start" :stroke "none" :fill "black" :font-size "small"}} s])

(defn normal-spaces []
  (apply concat
         (for [i (range 1 24)]
           (when-let [space-data (get-in @app-state [:board i])]
             (let [position (get piece-positions i)
                   space (normal-space (:x position) (:y position))
                   text (space-text (:text-x position) (:text-y position) (name (:icon space-data)))]
               [space text]
               )))))

(def rest-of-board
  [:svg
      {:view-box "0 0 1000 750"
       :width 1000
       :height 750}

      ;; space 1: bottle
      [:rect {:x (to-scale 50) :y (to-scale 60) :width (to-scale 40) :height (to-scale 30) :stroke "black" :stroke-width "0.5" :fill "lightgray"}]
      [:text {:x (to-scale 50) :y (to-scale 75) :style {:text-anchor "start" :stroke "none" :fill "black" :font-size "small"}} "bottle"]


      ;; space 2: gun
      [:rect {:x (to-scale 90) :y (to-scale 60) :width (to-scale 40) :height (to-scale 30) :stroke "black" :stroke-width "0.5" :fill "lightgray"}]
      [:text {:x (to-scale 90) :y (to-scale 75) :style {:text-anchor "start" :stroke "none" :fill "black" :font-size "small"}} "gun"]
      [:circle {:cx (to-scale 85) :cy (to-scale 65) :r (to-scale 4) :fill "orange"}]
      [:circle {:cx (to-scale 85) :cy (to-scale 75) :r (to-scale 4) :fill "black"}]

      ;; space 3: hat
      [:rect {:x (to-scale 130) :y (to-scale 60) :width (to-scale 40) :height (to-scale 30) :stroke "black" :stroke-width "0.5" :fill "lightgray"}]
      [:text {:x (to-scale 130) :y (to-scale 75) :style {:text-anchor "start" :stroke "none" :fill "black" :font-size "small"}} "hat"]
      [:circle {:cx (to-scale 125) :cy (to-scale 65) :r (to-scale 4) :fill "orange"}]

      ;; space 4: key
      [:rect {:x (to-scale 170) :y (to-scale 60) :width (to-scale 40) :height (to-scale 30) :stroke "black" :stroke-width "0.5" :fill "lightgray"}]
      [:text {:x (to-scale 170) :y (to-scale 75) :style {:text-anchor "start" :stroke "none" :fill "black" :font-size "small"}} "key"]
      [:circle {:cx (to-scale 165) :cy (to-scale 65) :r (to-scale 4) :fill "orange"}]
      [:circle {:cx (to-scale 165) :cy (to-scale 75) :r (to-scale 4) :fill "orange"}]
      [:circle {:cx (to-scale 165) :cy (to-scale 85) :r (to-scale 4) :fill "black"}]


      ;; space 5: knife
      [:rect {:x (to-scale 210) :y (to-scale 60) :width (to-scale 40) :height (to-scale 30) :stroke "black" :stroke-width "0.5" :fill "lightgray"}]
      [:text {:x (to-scale 210) :y (to-scale 75) :style {:text-anchor "start" :stroke "none" :fill "black" :font-size "small"}} "knife"]

      ;; space 6: skull
      [:rect {:x (to-scale 250) :y (to-scale 60) :width (to-scale 40) :height (to-scale 30) :stroke "black" :stroke-width "0.5" :fill "lightgray"}]
      [:text {:x (to-scale 250) :y (to-scale 75) :style {:text-anchor "start" :stroke "none" :fill "black" :font-size "small"}} "skull"]
      ])

(defn main-view []
  [:center
   [:h1 "CARTAGENA"
    [:div
     (-> [:svg
          {:view-box "0 0 1000 750"
           :width 1000
           :height 750}]
         (into (static-board))
         (into (jail))
         (into (normal-spaces))
         )]]])

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
