(ns alex-silva-music.db)

(def base-url "https://dl.dropboxusercontent.com/u/12514699/alex-silva-music/")
(def base-score-url (str base-url "scores/"))
(def base-music-url (str base-url "music/"))
(def base-sc-url "https://soundcloud.com/faceofman/")

(defn get-track-link [track-id]
  (str base-music-url track-id ".mp3"))

(def face-of-man-quintet-creds
  (array-map "Nick Bazzano" ["Alto Sax"]
             "Daro Behroozi" ["Bass Clarinet"]
             "Alex Silva" ["Guitar Electric"]
             "Charlie Hack" ["Double Bass"]
             "Jesse Chevan" ["Drums"]))

(def default-db
  {:collections          (array-map :some-recent-stuff {:credits [{"Alex Silva" ["vocals" "guitar"]}]}

                                    :at-the-pheelharmonic {:year    2012
                                                           :credits [{"Alex Silva" ["lead vocals" "guitar" "synths" "electronics"]}
                                                                     {"Coleman Moore" ["deep vocals" "guitar"]}
                                                                     {"Lara Andersson" ["lady vocals"]}
                                                                     {"Doug Berns" ["electric bass"]}
                                                                     {"Jesse Chevan" ["drums"]}]}

                                    :face-of-man {:year    2011
                                                  :credits (array-map "Alex Silva" ["lead vocals" "guitar" "synths" "electronics" "electric bass"]
                                                                      "Jesse Chevan" ["drums"]
                                                                      "Coleman Moore" ["mixing engineer" "drum programming on \"A Sharper Image\""]
                                                                      "Jojo Samuels" ["lady vocals on \"Future Half\""])})

   :tracks               (array-map :planes {:collection :some-recent-stuff
                                             :category   :face-of-man}

                                    :bouquet {:collection :some-recent-stuff
                                              :category   :face-of-man
                                              :liked      true}

                                    :altiloquence {:collection :at-the-pheelharmonic
                                                   :category   :face-of-man}

                                    :fast-car {:collection :at-the-pheelharmonic
                                               :category   :face-of-man}

                                    :like-devils-fly {:collection :at-the-pheelharmonic
                                                      :category   :face-of-man}

                                    :a-sharper-image {:collection :face-of-man
                                                      :category   :face-of-man}

                                    :ethnopoetics {:collection :face-of-man
                                                   :category   :face-of-man}

                                    :future-half {:collection :face-of-man
                                                  :category   :face-of-man}

                                    :le-chat-roulette {:category  :other
                                                       :year      2010
                                                       :performer :face-of-man-quintet
                                                       :credits   face-of-man-quintet-creds}

                                    :mr-silvas-magnet-school {:display-name "Mr Silva's Magnet School"
                                                              :category     :other
                                                              :year         2010
                                                              :group        :face-of-man-quintet
                                                              :credits      face-of-man-quintet-creds}

                                    :i-dalliance {:category :other
                                                  :year     2013
                                                  :credits  {"Alex Silva" ["programming"]}
                                                  :liked    true}

                                    :ii-convergence {:category :other
                                                     :year     2013
                                                     :credits  {"Alex Silva" ["programming"]}}

                                    :antisense {:category :other
                                                :year     2011
                                                :credits  (array-map "Yurie Mitsuhashi" ["violin"]
                                                                     "Isabel Gehweiler" ["cello"]
                                                                     "Joe Mohan" ["piano"])}

                                    :cool-runnings {:category  :other
                                                    :year      2010
                                                    :performer :counter-induction
                                                    :credits   (array-map "Margaret Lancaster" ["flute"]
                                                                          "Benjamin Fingland" ["clarinet"]
                                                                          "Miranda Cuckson" ["violin"]
                                                                          "Sumire Kudo" ["cello"]
                                                                          "Steve Beck" ["piano"]
                                                                          "Carl Christian Bettendorf" ["conductor"])})

   :links                (array-map :soundcloud base-sc-url
                                    :spotify "https://player.spotify.com/artist/6FMeK8Rk2ZAATsnr6qd4XP"
                                    :bandcamp "https://faceofman.bandcamp.com/"
                                    :facebook "https://www.facebook.com/faceofmanband/"
                                    :twitter "https://twitter.com/faceofmanband"
                                    :itunes "https://itunes.apple.com/us/artist/face-of-man/id441404508")
   :active-panel         :face-of-man
   :active-collection-id nil
   :active-track-id      nil
   :playing-track        {:track-id :bouquet
                          :state    nil}
   })

(defn is-collection [collection-id]
  (not (nil? (-> default-db :collections collection-id))))

(defn get-panels []
  [:face-of-man :other :links :likes])