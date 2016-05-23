(ns alex-silva-music.handlers
  (:require [re-frame.core :refer [register-handler path dispatch after]]
            [alex-silva-music.db :refer [default-db ls->favorite-tracks favorite-tracks->ls! schema PlayingTrack] :as db]
            [schema.core :as s :include-macros true]))

;; -- Custom Middleware ----------------------------------------------------------
;;
;;
;;

(defn check-and-throw
  "throw an exception if db doesn't match the schema."
  [a-schema db]
  (if-let [problems (s/check a-schema db)]
    (throw (js/Error. (str "schema check failed: " problems)))))

(def check-schema-mw
  "after an event handler has run, this middleware can check that the value in app-db still correctly matches the schema."
  (after (partial check-and-throw schema)))

;; middleware to store todos into local storage
(def ->ls (after favorite-tracks->ls!))

;; -- Helper functions ----------------------------------------------------------
;;
;;
;;

(defn set-active-collection [current-collection-id [_ new-collection-id]]
  (if (= current-collection-id new-collection-id)
    nil
    new-collection-id))

(defn toggle-track-favorited [db [_ track-id]]
  (let [favorites (:favorites db)
        updated-favorites (if (some #(= track-id %) favorites)
                    (into [] (filter #(not (= % track-id)) favorites))
                    (conj favorites track-id))]
    (assoc db :favorites updated-favorites :track-favorite-toggled? true)))

(s/defn toggle-playing-track-state :- (s/maybe PlayingTrack)
  [current-playing-track-info :- (s/maybe PlayingTrack) & _]
  (if current-playing-track-info
    (assoc current-playing-track-info :state (if (= (:state current-playing-track-info) :play)
                                              :pause
                                              :play)
                                     :load? false)))

(defn set-playing-track [current-playing-track-info [_ new-playing-track-id]]
  (if (= (:track-id current-playing-track-info) new-playing-track-id)
    (toggle-playing-track-state current-playing-track-info)
    (let [track-url (get-in default-db [:tracks new-playing-track-id :url])]
      (PlayingTrack. new-playing-track-id track-url :play true))))

;; -- Handlers ----------------------------------------------------------
;;
;;
;;

(register-handler
  :initialize-db
  check-schema-mw
  (fn [_ _]
    (merge default-db (ls->favorite-tracks))))

(register-handler
  :set-active-panel
  [check-schema-mw (path :active-panel)]
  (fn [_ [_ new-panel]]
    new-panel))

(register-handler
  :set-active-project
  [check-schema-mw (path :active-project-id)]
  (fn [_ [_ new-project]]
    new-project))

(register-handler
  :set-active-collection
  [check-schema-mw (path :active-collection-id)]
  set-active-collection)

(register-handler
  :toggle-track-favorited
  [check-schema-mw ->ls]
  toggle-track-favorited)

(register-handler
  :set-playing-track
  [check-schema-mw (path :playing-track)]
  set-playing-track)

(register-handler
  :toggle-playing-track-state
  [check-schema-mw (path :playing-track)]
  toggle-playing-track-state)

(register-handler
  :reset-track-favorite-toggled
  [check-schema-mw (path :track-favorite-toggled?)]
  (fn [_ _]
    false))
