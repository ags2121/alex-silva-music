(ns alex-silva-music.subs
  (:require [re-frame.core :as re-frame]
            [schema.core :as s :include-macros true]
            [alex-silva-music.db :as db])
  (:require-macros [reagent.ratom :refer [reaction]]))

;; -- Helper functions ----------------------------------------------------------
;;
;;
;;

(s/defn get-favorite-tracks :- [(s/map-entry s/Keyword db/Track)]
  [db :- db/schema]
  (let [favorite-tracks-set (set (:favorites db))]
    (filter #(contains? favorite-tracks-set (-> % key)) (:tracks db))))

(s/defn get-collection :- (merge db/Collection {:tracks [(s/->MapEntry s/Keyword db/Track)]})
  [db :- db/schema
   collection-name :- db/CollectionName]
  (let [collection-data (collection-name (:collections db))
        tracks-for-collection (into [] (filter #(= (-> % val :collection) collection-name) (:tracks db)))]
    (assoc collection-data :tracks tracks-for-collection)))

;; -- Subscription handlers and registration ----------------------------------------------------------
;;
;;
;;

(re-frame/register-sub
  :collection
  (fn [db [_ collection-name]]
    (reaction (get-collection @db collection-name))))

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
  :favorite-tracks
  (fn [db _]
    (reaction (get-favorite-tracks @db))))

(re-frame/register-sub
  :is-favorite
  (fn [db [_ track-id]]
    (reaction (->> @db
                   :favorites
                   (some #(= track-id %))))))

(re-frame/register-sub
  :track-favorite-toggled?
  (fn [db _]
    (reaction (:track-favorite-toggled? @db))))
