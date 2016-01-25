(ns cartagena.server
  (:require
    [compojure.core :refer :all]
    [compojure.route :as route]
    [ring.middleware.cors :as cors]
    [ring.middleware.transit :as trans]
    [ring.util.response :as response]
    [cartagena.core :as engine]))

(defroutes app-routes
           (GET "/" [] "<h1>Hello World</h1>")

           (GET "/fake-game" []
             (response/response (engine/new-game! [{:name "tanya" :color :orange} {:name "rusty" :color :black}])))

           (POST "/new-game" req
             (let [players (get (:params req) :players)]
               (engine/new-game! players)))

           (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> app-routes
      (cors/wrap-cors :access-control-allow-origin [#"http://localhost:3449"]
                      :access-control-allow-methods [:get :put :post :delete])
      (trans/wrap-transit-response {:encoding :json :keywords? true :opts {}})
      (trans/wrap-transit-body {:keywords? true :encoding :json, :opts {}})))

