(ns cartagena-web.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom nil))

(defn main-view []
  [:center
   [:h1 "CARTAGENA"
    [:div
     [:svg
      {:view-box "0 0 1000 800"
       :width 1000
       :height 800}
      [:rect {:x 0 :y 0 :width 1000 :height 800 :stroke "black" :fill "none"}]]]]])

(defn ^:export main []
  (when-let [app (. js/document (getElementById "app"))]
    (reagent/render-component [main-view] app)))

(main)

(defn on-js-reload []
  (main))
