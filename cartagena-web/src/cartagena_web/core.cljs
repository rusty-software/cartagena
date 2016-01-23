(ns ^:figwheel-always cartagena-web.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom nil))

(defn jail-img []
  [:img {:src "img/jail.png" :width 30 :height 30 :left 0 :top 0}])

(defn jail []
  [:rect {:x 0 :y 0 :width 50 :height 90 :stroke "black" :fill "brown"}])

(defn main-view []
  [:center
   [:h1 "CARTAGENA"
    [:div
     [:svg
      {:view-box "0 0 800 600"
       :width 800
       :height 600}
      [:rect {:x 0 :y 0 :width 800 :height 600 :stroke "black" :stroke-width "1" :fill "tan"}]
      [:rect {:x 0 :y 0 :width 500 :height 400 :stroke "black" :stroke-width "0.5" :fill "burlywood"}]
      (jail)
      (jail-img)]]]])

(defn ^:export main []
  (when-let [app (. js/document (getElementById "app"))]
    (reagent/render-component [main-view] app)))

(main)

(defn on-js-reload []
  (main))
