(ns alex-silva-music.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [alex-silva-music.handlers]
              [alex-silva-music.subs]
              [alex-silva-music.routes :as routes]
              [alex-silva-music.views :as views]
              [alex-silva-music.config :as config]))

(when config/debug?
  (println "dev mode"))

(defn mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init [] 
  (routes/app-routes)
  (re-frame/dispatch-sync [:initialize-db])
  (mount-root))
