(ns alex-silva-music.handlers
  (:require [re-frame.core :refer [register-handler path dispatch]]
            [alex-silva-music.db :refer [default-db ls->favorite-tracks favorite-tracks->ls! schema]]
            [schema.core :as s]))

;; -- Custom Middleware ----------------------------------------------------------
;;
;;
;;

;(defn check-and-throw
;  "throw an exception if db doesn't match the schema."
;  [a-schema db]
;  (if-let [problems (s/check a-schema db)]
;    (throw (js/Error. (str "schema check failed: " problems)))))
;
;;; after an event handler has run, this middleware can check that
;;; the value in app-db still correctly matches the schema.
;(def check-schema-mw (after (partial check-and-throw schema)))
;
;;; middleware to store todos into local storage
;(def ->ls (after favorite-tracks->ls!))

;; -- Handlers ----------------------------------------------------------
;;
;;
;;

(register-handler
  :initialize-db
  (fn [_ _]
    default-db))

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
  :toggle-track-favorited
  (path :tracks)
  (fn [tracks [_ track-id]]
    (dispatch [:track-favorite-toggled? true])
    (let [updated-track (update-in (track-id tracks) [:liked] not)]
      (assoc tracks track-id updated-track))))

(register-handler
  :set-playing-track
  (path :playing-track)
  (fn [current-playing-track-info [_ new-playing-track-id new-state]]
    (if (= (:track-id current-playing-track-info) new-playing-track-id)
      (assoc current-playing-track-info :state (if (= (:state current-playing-track-info) :play)
                                                 :pause
                                                 :play)
                                        :load? false)
      (let [track-url (-> default-db :tracks new-playing-track-id :url)
            state (if new-state new-state :play)]
        {:track-id new-playing-track-id :url track-url :state state :load? true}))))

(register-handler
  :track-favorite-toggled?
  (path :track-favorite-toggled?)
  (fn [_ [_ new-state]]
    new-state))
