(ns cartagena.server
  (:require
    [compojure.core :refer :all]
    [compojure.route :as route]
    [ring.middleware.cors :as cors]
    [ring.middleware.transit :as trans]
    [ring.util.response :as response]
    [cartagena.core :as engine]))

(def default-players [{:name "tanya" :color :orange} {:name "rusty" :color :black}])
(defroutes app-routes
           (GET "/" [] "<h1>Hello World</h1>")

           (POST "/update-active-player" req
             (let [{:keys [actions-remaining current-player player-order]} (:body req)]
               (response/response (engine/update-current-player actions-remaining current-player player-order))))

           (POST "/play-card" req
             (let [{:keys [player icon from-space board discard-pile]} (:body req)]
               (response/response (engine/play-card player icon from-space board discard-pile))))

           (POST "/move-back" req
             (let [{:keys [player from-space board draw-pile discard-pile]} (:body req)]
               (response/response (engine/move-back player from-space board draw-pile discard-pile))))

           (POST "/new-game" req
             (if-let [players (get (:body req) :players)]
               (response/response (engine/new-game! players))
               (response/response (engine/new-game! default-players))))

           (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> app-routes
      (cors/wrap-cors :access-control-allow-origin [#"http://localhost:3449"]
                      :access-control-allow-methods [:get :put :post :delete])
      (trans/wrap-transit-response {:encoding :json :keywords? true :opts {}})
      (trans/wrap-transit-body {:keywords? true :encoding :json, :opts {}})))

