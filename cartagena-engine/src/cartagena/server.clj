(ns cartagena.server
  (:require
    [compojure.core :refer :all]
    [compojure.route :as route]
    [ring.middleware.cors :as cors]
    [ring.middleware.defaults :as defaults]
    [ring.middleware.json :as json]
    [ring.util.response :as response]
    [camel-snake-kebab.core :as csk]
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
  (-> (defaults/wrap-defaults app-routes defaults/site-defaults)
      (cors/wrap-cors :access-control-allow-origin [#"http://localhost:3449"]
                      :access-control-allow-methods [:get :put :post :delete])
      (json/wrap-json-body)
      (json/wrap-json-response {:key-fn csk/->camelCaseString})))

