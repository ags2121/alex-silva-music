(ns alex-silva-music.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]))

;; -- Helper functions ----------------------------------------------------------
;;
;;
;;

(defn liked-tracks
  "return the tracks for which :liked is true"
  [db]
  (filter #(-> % val :liked) (:tracks db)))

(defn favorited-tracks
  [db]
  (:favorites db))

;; -- Subscription handlers and registration ----------------------------------------------------------
;;
;;
;;

(re-frame/register-sub
  :collection
  (fn [db [_ collection-name]]
    (let [collection-data (collection-name (:collections @db))
          tracks-for-collection (into [] (filter #(= (-> % val :collection) collection-name) (:tracks @db)))
          collection-data-with-tracks (assoc collection-data :tracks tracks-for-collection)]
      (reaction collection-data-with-tracks))))

(re-frame/register-sub
  :active-panel
  (fn [db _]
    (reaction (:active-panel @db))))

(re-frame/register-sub
  :active-project-id
  (fn [db _]
    (reaction (:active-project-id @db))))

(re-frame/register-sub
  :active-collection-id
  (fn [db _]
    (reaction (:active-collection-id @db))))

(re-frame/register-sub
  :active-track-id
  (fn [db _]
    (reaction (:active-track-id @db))))

(re-frame/register-sub
  :playing-track
  (fn [db _]
    (reaction (:playing-track @db))))

(re-frame/register-sub
  :liked-tracks
  (fn [db _]
    (reaction (liked-tracks @db))))

(re-frame/register-sub
  :favorite-tracks
  (fn [db _]
    (reaction (liked-tracks @db))))

(re-frame/register-sub
  :is-liked
  (fn [db [_ track-id]]
    (reaction (-> @db :tracks track-id :liked))))

(re-frame/register-sub
  :is-favorite
  (fn [db [_ track-id]]
    (reaction (-> @db :favorites (contains? track-id)))))

(re-frame/register-sub
  :track-favorite-toggled?
  (fn [db _]
    (reaction (-> @db :track-favorite-toggled?))))
