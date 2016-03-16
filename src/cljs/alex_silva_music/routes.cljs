(ns alex-silva-music.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.History)
  (:require [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [re-frame.core :refer [dispatch]]
            [alex-silva-music.db :refer [is-project?]]))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      EventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn convert-strs-to-keywords [params]
  (reduce #(assoc %1 (key %2) (-> %2 val keyword)) {} params))

(defn set-complete-path [params]
  (.log js/console params)
  (dispatch [:set-complete-path (convert-strs-to-keywords params)]))

(defn app-routes []
  (secretary/set-config! :prefix "#")

  (defroute "/:panel" {:as params}
            (set-complete-path params))

  (defroute "/:panel/:project-or-track" {:as params}
            (let [is-project (is-project? (:project-or-track params))
                  params (assoc params
                           (if is-project :project :track)
                           (:project-or-track params))]
              (set-complete-path params)))

  ;; --------------------
  (hook-browser-navigation!))
