(ns ^:figwheel-always cartagena-web.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom nil))

(defn to-scale [n]
  (* 1.5 n))

(defn main-view []
  [:center
   [:h1 "CARTAGENA"
    [:div
     [:svg
      {:view-box "0 0 1000 750"
       :width 1000
       :height 750}
      ;; play mat
      [:rect {:x 0 :y 0 :width 1000 :height 750 :stroke "black" :stroke-width "1" :fill "tan"}]

      ;; board
      [:rect {:x 0 :y 0 :width (to-scale 500) :height (to-scale 400) :stroke "black" :stroke-width "0.5" :fill "burlywood"}]

      ;; jail
      [:rect {:x 0 :y 0 :width (to-scale 50) :height (to-scale 90) :stroke "black" :fill "darkgray"}]
      [:text {:x 0 :y (to-scale 15) :style {:text-anchor "start" :stroke "none" :fill "black" :font-size "smaller"}} "jail"]

      ;; players in jail
      [:circle {:cx (to-scale 5) :cy (to-scale 35) :r (to-scale 4) :fill "orange"}]
      [:circle {:cx (to-scale 5) :cy (to-scale 45) :r (to-scale 4) :fill "orange"}]
      [:circle {:cx (to-scale 15) :cy (to-scale 35) :r (to-scale 4) :fill "green"}]
      [:circle {:cx (to-scale 15) :cy (to-scale 45) :r (to-scale 4) :fill "green"}]
      [:circle {:cx (to-scale 15) :cy (to-scale 55) :r (to-scale 4) :fill "green"}]
      [:circle {:cx (to-scale 15) :cy (to-scale 65) :r (to-scale 4) :fill "green"}]
      [:circle {:cx (to-scale 15) :cy (to-scale 75) :r (to-scale 4) :fill "green"}]
      [:circle {:cx (to-scale 15) :cy (to-scale 85) :r (to-scale 4) :fill "green"}]
      [:circle {:cx (to-scale 25) :cy (to-scale 35) :r (to-scale 4) :fill "black"}]
      [:circle {:cx (to-scale 25) :cy (to-scale 45) :r (to-scale 4) :fill "black"}]
      [:circle {:cx (to-scale 25) :cy (to-scale 55) :r (to-scale 4) :fill "black"}]
      [:circle {:cx (to-scale 25) :cy (to-scale 65) :r (to-scale 4) :fill "black"}]


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
      ]]]])

(defn ^:export main []
  (when-let [app (. js/document (getElementById "app"))]
    (reagent/render-component [main-view] app)))

(main)

(defn on-js-reload []
  (main))
