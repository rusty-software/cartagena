(ns cartagena.server
  (:require
    [compojure.core :refer :all]
    [compojure.route :as route]
    [ring.middleware.defaults :as defaults]))

(defroutes app-routes
           (GET "/" [] "<h1>Hello World</h1>")
           (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> (defaults/wrap-defaults app-routes defaults/site-defaults)))

