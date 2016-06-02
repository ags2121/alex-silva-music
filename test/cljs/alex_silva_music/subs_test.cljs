(ns alex-silva-music.subs-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [alex-silva-music.subs :as s]
            [alex-silva-music.db :as db]
            [schema.test :as st]))

(use-fixtures :once st/validate-schemas)

(deftest get-favorite-tracks-with-default-db-return-default-favorite-tracks
  (is (= '([:planes {:collection :recent-work,
                     :project    :face-of-man,
                     :soundcloud "https://soundcloud.com/faceofman/planes",
                     :url        "https://dl.dropboxusercontent.com/u/12514699/alex-silva-music/music/planes.mp3"}]
            [:like-devils-fly {:collection :at-the-pheelharmonic,
                               :project    :face-of-man,
                               :soundcloud "https://soundcloud.com/faceofman/like-devils-fly",
                               :url        "https://dl.dropboxusercontent.com/u/12514699/alex-silva-music/music/like-devils-fly.mp3"}]
            [:ethnopoetics {:collection :face-of-man,
                            :project    :face-of-man,
                            :soundcloud "https://soundcloud.com/faceofman/ethnopoetics",
                            :url        "https://dl.dropboxusercontent.com/u/12514699/alex-silva-music/music/ethnopoetics.mp3"}]
            [:la-chat-roulette {:project    :compositions,
                                :year       2010,
                                :performer  :face-of-man-quintet,
                                :credits    {"Nick Bazzano" ["Alto Sax"], "Daro Behroozi" ["Bass Clarinet"], "Alex Silva" ["Guitar Electric"], "Charlie Hack" ["Double Bass"], "Jesse Chevan" ["Drums"]},
                                :soundcloud "https://soundcloud.com/faceofman/la-chat-roulette",
                                :url        "https://dl.dropboxusercontent.com/u/12514699/alex-silva-music/music/la-chat-roulette.mp3",
                                :score      "https://dl.dropboxusercontent.com/u/12514699/alex-silva-music/scores/la-chat-roulette.pdf"}]
            [:i-dalliance {:project    :compositions,
                           :year       2013,
                           :credits    {"Alex Silva" ["programming"]},
                           :soundcloud "https://soundcloud.com/faceofman/i-dalliance",
                           :url        "https://dl.dropboxusercontent.com/u/12514699/alex-silva-music/music/i-dalliance.mp3",
                           :score      "https://dl.dropboxusercontent.com/u/12514699/alex-silva-music/scores/i-dalliance.pdf"}])
         (s/get-favorite-tracks db/default-db))))

(deftest get-favorite-tracks-if-no-favorite-tracks-return-empty-seq
  (is (= '()
         (s/get-favorite-tracks (assoc db/default-db :favorites [])))))

(deftest get-collection-return-collection
  (is (= {:year    2012,
          :credits {"Alex Silva"     ["lead vocals" "guitar" "synths" "electronics"],
                    "Coleman Moore"  ["deep vocals" "guitar"],
                    "Lara Andersson" ["lady vocals"],
                    "Doug Berns"     ["electric bass"], "Jesse Chevan" ["drums"]},
          :tracks  [[:like-devils-fly {:collection :at-the-pheelharmonic,
                                       :project    :face-of-man,
                                       :soundcloud "https://soundcloud.com/faceofman/like-devils-fly",
                                       :url        "https://dl.dropboxusercontent.com/u/12514699/alex-silva-music/music/like-devils-fly.mp3"}]
                    [:altiloquence {:collection :at-the-pheelharmonic,
                                    :project    :face-of-man,
                                    :soundcloud "https://soundcloud.com/faceofman/altiloquence",
                                    :url        "https://dl.dropboxusercontent.com/u/12514699/alex-silva-music/music/altiloquence.mp3"}]
                    [:fast-car {:collection :at-the-pheelharmonic,
                                :project    :face-of-man,
                                :soundcloud "https://soundcloud.com/faceofman/fast-car",
                                :url        "https://dl.dropboxusercontent.com/u/12514699/alex-silva-music/music/fast-car.mp3"}]]}
         (s/get-collection db/default-db :at-the-pheelharmonic))))

(deftest get-collection-input-invalid-collection-throw-exception
  (is (thrown? js/Error
         (s/get-collection db/default-db :123))))

(deftest is-favorite-return-true
  (is (= true
         (s/is-favorite db/default-db :like-devils-fly))))

(deftest is-favorite-return-false
  (is (= false
         (s/is-favorite db/default-db :ii-convergence))))
