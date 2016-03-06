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

  (defroute "/:panel/:collection-or-track" [panel collection-or-track]
            (let [panel (keyword panel)
                  collection-or-track (keyword collection-or-track)]
              (dispatch [:set-active-panel panel])
              (let [dispatch-fn-id (if (db/is-collection collection-or-track)
                                     :set-active-collection
                                     :set-active-track)]
                (dispatch [dispatch-fn-id collection-or-track]))))

  (defroute "/:panel/:collection/:track" [panel collection track]
            (dispatch [:set-active-panel (keyword panel)])
            (dispatch [:set-active-collection (keyword collection)])
            (dispatch [:set-active-track (keyword track)]))

  ;; --------------------
  (hook-browser-navigation!))
