(ns alex-silva-music.handlers-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [alex-silva-music.handlers :as h]
            [alex-silva-music.db :as db]
            [schema.test :as st]))

(use-fixtures :once st/validate-schemas)

(def playing-track (db/PlayingTrack. :planes "url" :play true))

(deftest set-active-collection-when-same-collection-return-nil
  (is (= nil
         (h/set-active-collection :face-of-man [:set-active-collection :face-of-man]))))

(deftest set-active-collection-when-different-collection-return-collection
  (is (= :face-of-man
         (h/set-active-collection :at-the-pheelharmonic [:set-active-collection :face-of-man]))))

(deftest toggle-track-favorited-when-track-is-favorited-then-unfavorite-track
  (is (= []
         (:favorites
           (h/toggle-track-favorited (assoc db/default-db :favorites [:planes]) [:toggle-track-favorited :planes])))))

(deftest toggle-track-favorited-when-track-is-not-favorited-then-favorite-track
  (is (= [:planes]
         (:favorites
           (h/toggle-track-favorited (assoc db/default-db :favorites []) [:toggle-track-favorited :planes])))))

(deftest toggle-track-favorited-set-track-favorite-toggled?-to-true
  (is (= true
         (:track-favorite-toggled?
           (h/toggle-track-favorited db/default-db [:toggle-track-favorited :planes])))))

(deftest toggle-playing-track-state-if-current-track-is-nil-return-nil
  (is (= nil
         (h/toggle-playing-track-state nil))))

(deftest set-playing-track-if-track-is-already-do-not-change-track-id
  (is (= :planes
         (:track-id (h/set-playing-track playing-track [:set-playing-track :planes])))))
;
(deftest set-playing-track-if-track-is-already-playing-then-pause
  (is (= :pause
         (:state (h/set-playing-track playing-track [:set-playing-track :planes])))))

(deftest set-playing-track-if-track-is-already-playing-then-set-load?-to-false
  (is (= false
         (:load? (h/set-playing-track playing-track [:set-playing-track :planes])))))

(deftest set-playing-track-if-track-is-paused-then-do-not-change-track-id
  (is (= :planes
         (:track-id (h/set-playing-track (assoc playing-track :state :pause) [:set-playing-track :planes])))))

(deftest set-playing-track-if-track-is-paused-then-then-play-track
  (is (= :play
         (:state (h/set-playing-track (assoc playing-track :state :pause) [:set-playing-track :planes])))))

(deftest set-playing-track-if-track-is-paused-then-then-set-load?-to-false
  (is (= false
         (:load? (h/set-playing-track (assoc playing-track :state :pause) [:set-playing-track :planes])))))

(deftest set-playing-track-if-playing-new-track-then-set-new-track-id
  (is (= :altiloquence
         (:track-id (h/set-playing-track playing-track [:set-playing-track :altiloquence])))))

(deftest set-playing-track-if-playing-new-track-then-set-new-track-url
  (is (= (get-in db/default-db [:tracks :altiloquence :url])
         (:url (h/set-playing-track playing-track [:set-playing-track :altiloquence])))))

(deftest set-playing-track-if-playing-new-track-then-play-track
  (is (= :play
         (:state (h/set-playing-track playing-track [:set-playing-track :altiloquence])))))

(deftest set-playing-track-if-playing-new-track-then-set-load?-to-true
  (is (= true
         (:load? (h/set-playing-track playing-track [:set-playing-track :altiloquence])))))
