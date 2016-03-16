(ns alex-silva-music.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]
            [alex-silva-music.db :as db]))

;; -- Helpers -----------------------------------------------------------------

(defn liked-tracks
  "return the tracks for which :liked is true"
  [db]
  (filter #(-> % val :liked) (:tracks db)))

(defn get-new-path [current-path new-path-element new-path-element-type]
  (case new-path-element-type
    :panel
    (array-map :panel new-path-element)

    :project
    (-> (assoc current-path :project new-path-element)
        (assoc :collection nil)
        (assoc :track nil))

    :collection
    (-> (assoc current-path :collection new-path-element)
        (assoc :track nil))

    :track
    (-> (assoc current-path :track new-path-element))))

;; -- Subscription handlers and registration  ---------------------------------

(re-frame/register-sub
  :collections
  (fn [db _]
    (reaction (map key (:collections @db)))))

(re-frame/register-sub
  :path
  (fn [db _]
    (reaction (:path @db))))

(re-frame/register-sub
  :get-path
  (fn [db [_ new-path-element new-path-element-type]]
    (.log js/console new-path-element-type)
    (let [current-path (:path @db)
          new-path (get-new-path current-path new-path-element new-path-element-type)]
      (.log js/console new-path)
      (.log js/console (str (conj (vals new-path) :#)))
      (reaction
        (reduce
          #(str (name %1) "/" (name %2))
          (conj (remove nil? (vals new-path)) :#))))))

(re-frame/register-sub
  :tracks-by-category
  (fn [db [_ category]]
    (reaction (filter #(= (-> % val :project) category) (:tracks @db)))))

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
  :links
  (fn [db _]
    (reaction (:links @db))))

(re-frame/register-sub
  :liked-tracks
  (fn [db _]
    (reaction (liked-tracks @db))))

(re-frame/register-sub
  :is-liked
  (fn [db [_ track-id]]
    (reaction (-> @db :tracks track-id :liked))))

(re-frame/register-sub
  :menu-data
  (fn [db _]
    (let [liked-tracks-count (reaction (count (liked-tracks @db)))
          active-panel-id (reaction (:active-panel @db))]
      (reaction [@liked-tracks-count @active-panel-id]))))


