(ns alex-silva-music.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.History)
  (:require [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [re-frame.core :refer [dispatch]]
            [alex-silva-music.db :as db]))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      EventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn app-routes []
  (secretary/set-config! :prefix "#")

  (defroute "/:panel" [panel]
            (dispatch [:set-active-panel (keyword panel)]))

  (defroute "/projects/:project" [project]
            (dispatch [:set-active-project (keyword project)]))

  (defroute "/favorites/:track" [track]
            (dispatch [:set-active-track (keyword track)]))

  ;; --------------------
  (hook-browser-navigation!))
