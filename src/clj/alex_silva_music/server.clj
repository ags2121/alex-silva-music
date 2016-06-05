(ns alex-silva-music.server
  (:require [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojure.java.io :as io]
            [org.httpkit.client :as httpkit]
            [overtone.at-at :refer :all])
  (:gen-class))

(def my-pool (mk-pool))

(defn get-requests []
  (httpkit/get "http://www.alexsilvamusic.us" (fn [_] (println "server polled"))))

;; every half hour poll the server
(defn poll-server []
  (every 1800000 get-requests my-pool))

(defroutes routes
           (GET "/" _
             {:status  200
              :headers {"Content-Type" "text/html; charset=utf-8"}
              :body    (io/input-stream (io/resource "public/index.html"))})
           (resources "/"))

(def http-handler
  (-> routes
      (wrap-defaults api-defaults)
      wrap-with-logger
      wrap-gzip))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (run-jetty http-handler {:port port :join? false})
    (poll-server)))
