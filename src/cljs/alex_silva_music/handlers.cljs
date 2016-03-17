(ns alex-silva-music.handlers
    (:require [re-frame.core :refer [register-handler path]]
              [alex-silva-music.db :as db]))

(register-handler
 :initialize-db
 (fn  [_ _]
   (db/get-default-db)))

(register-handler
  :set-active-panel
  (path :active-panel)
  (fn [_ [_ new-panel]]
    new-panel))

(register-handler
  :set-active-project
  (path :active-project-id)
  (fn [_ [_ new-project]]
    new-project))

(register-handler
  :set-active-collection
  (path :active-collection-id)
  (fn [current-collection-id [_ new-collection-id]]
    (if (= current-collection-id new-collection-id)
      nil
      new-collection-id)))

(register-handler
  :set-track-liked
  (path :tracks)
  (fn [tracks [_ track-id]]
    (let [updated-track (update-in (track-id tracks) [:liked] not)]
      (assoc tracks track-id updated-track))))

(register-handler
  :set-active-track
  (path :active-track-id)
  (fn [current-track [_ new-track]]
    (if (= current-track new-track)
      nil
      new-track)))

(register-handler
  :set-playing-track
  (path :playing-track)
  (fn [current-playing-track-info [_ new-playing-track-id new-state]]
    (if (= (:track-id current-playing-track-info) new-playing-track-id)
      (assoc current-playing-track-info :state (if (= (:state current-playing-track-info) :play)
                                                 :pause
                                                 :play)
                                        :load? false)
      (let [track-url (-> (db/get-default-db) :tracks new-playing-track-id :url)
            state (if new-state new-state :play)]
        {:track-id new-playing-track-id :url track-url :state state :load? true}))))
