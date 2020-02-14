(ns alex-silva-music.db
  (:require [cljs.reader]
            [schema.core :as s :include-macros true]))

;; -- Schema -----------------------------------------------------------------
;;
;; This is a Prismatic Schema which documents the structure of app-db
;; See: https://github.com/Prismatic/schema
;;

(def Credits
  (s/conditional
    #(instance? PersistentArrayMap %)
    {s/Str [s/Str]}))

(def CollectionName (s/enum :bouquet :at-the-pheelharmonic :face-of-man :covers))

(def Collection {(s/optional-key :credits) Credits
                 (s/optional-key :year)    s/Int})

(def Track {:project                       (s/enum :face-of-man :compositions :personal-space)
            :url                           s/Str
            (s/optional-key :soundcloud)   s/Str
            (s/optional-key :bandcamp)     s/Str
            (s/optional-key :bandcamp-ps)  s/Str
            (s/optional-key :score)        s/Str
            (s/optional-key :display-name) s/Str
            (s/optional-key :collection)   CollectionName
            (s/optional-key :year)         s/Int
            (s/optional-key :credits)      Credits
            (s/optional-key :performer)    s/Keyword})


(s/defrecord PlayingTrack
  [track-id :- s/Keyword
   url :- s/Str
   state :- (s/enum :play :pause)
   load? :- s/Bool
   display-name :- s/Str])

(def schema {:collections                              (s/conditional
                                                         #(instance? PersistentArrayMap %)
                                                         {CollectionName {(s/optional-key :credits) Credits
                                                                          (s/optional-key :year)    s/Int}})
             :tracks                                   (s/conditional
                                                         #(instance? PersistentArrayMap %)
                                                         {s/Keyword Track})
             :links                                    (s/conditional
                                                         #(instance? PersistentArrayMap %)
                                                         {s/Keyword s/Str})
             :favorites                                [s/Keyword]
             (s/optional-key :track-favorite-toggled?) s/Bool
             :active-panel                             (s/maybe s/Keyword)
             :active-project-id                        (s/maybe s/Keyword)
             :active-collection-id                     (s/maybe s/Keyword)
             :playing-track                            (s/maybe PlayingTrack)})

;; -- "Private namespace vars" ----------------------------------------------------------
;;
;;
;;

(def ^:private base-url "https://s3.amazonaws.com/alexsilvamusic/")
(def ^:private base-score-url (str base-url "scores/"))
(def ^:private base-music-url (str base-url "music/"))
(def ^:private base-sc-url "https://soundcloud.com/faceofman/")
(def ^:private base-bc-url "https://faceofman.bandcamp.com/")
(def ^:private base-ps-bc-url "https://personalspaceband.bandcamp.com/")
(def ^:private face-of-man-quintet-creds
  (array-map "Nick Bazzano" ["Alto Sax"]
             "Daro Behroozi" ["Bass Clarinet"]
             "Alex Silva" ["Guitar Electric"]
             "Charlie Hack" ["Double Bass"]
             "Jesse Chevan" ["Drums"]))

(def ^:private base-db
  {:collections          (array-map :bouquet {:year 2017}

                                    :covers {:credits (array-map "Alex Silva" ["vocals" "guitar"])}

                                    :at-the-pheelharmonic {:year    2012
                                                           :credits (array-map "Alex Silva" ["lead vocals" "guitar" "synths" "electronics"]
                                                                               "Coleman Moore" ["deep vocals" "guitar"]
                                                                               "Lara Andersson" ["lady vocals"]
                                                                               "Doug Berns" ["electric bass"]
                                                                               "Jesse Chevan" ["drums"])}

                                    :face-of-man {:year    2011
                                                  :credits (array-map "Alex Silva" ["lead vocals" "guitar" "synths" "electronics" "electric bass"]
                                                                      "Jesse Chevan" ["drums"]
                                                                      "Coleman Moore" ["mixing engineer" "drum programming on \"A Sharper Image\""]
                                                                      "Jojo Samuels" ["lady vocals on \"Future Half\""])})

   :tracks               (array-map :lavender {:collection :bouquet
                                               :project    :face-of-man}

                                    :her-vernacular {:collection :bouquet
                                                     :project    :face-of-man}                                    

                                    :same-dream {:collection :bouquet
                                                 :project    :face-of-man}

                                    :im-a-flirt {:collection   :covers
                                                 :project      :face-of-man
                                                 :display-name "I'm a Flirt"}

                                    :planes {:collection :covers
                                             :project    :face-of-man}

                                    :like-devils-fly {:collection :at-the-pheelharmonic
                                                      :project    :face-of-man}

                                    :altiloquence {:collection :at-the-pheelharmonic
                                                   :project    :face-of-man}

                                    ; :fast-car {:collection :at-the-pheelharmonic
                                    ;            :project    :face-of-man}

                                    :ethnopoetics {:collection :face-of-man
                                                   :project    :face-of-man}

                                    :a-sharper-image {:collection :face-of-man
                                                      :project    :face-of-man}

                                    :future-half {:collection :face-of-man
                                                  :project    :face-of-man}

                                    :la-chat-roulette {:project   :compositions
                                                       :year      2010
                                                       :performer :face-of-man-quintet
                                                       :credits   face-of-man-quintet-creds}

                                    :mr-silvas-magnet-school {:display-name "Mr Silva's Magnet School"
                                                              :project      :compositions
                                                              :year         2010
                                                              :performer    :face-of-man-quintet
                                                              :credits      face-of-man-quintet-creds}

                                    :i-dalliance {:project :compositions
                                                  :year    2013
                                                  :credits {"Alex Silva" ["programming"]}}

                                    :ii-convergence {:project :compositions
                                                     :year    2013
                                                     :credits {"Alex Silva" ["programming"]}}

                                    :antisense {:project :compositions
                                                :year    2011
                                                :credits (array-map "Yurie Mitsuhashi" ["violin"]
                                                                    "Isabel Gehweiler" ["cello"]
                                                                    "Joe Mohan" ["piano"])}

                                    :cool-runnings {:project   :compositions
                                                    :year      2010
                                                    :performer :counter-induction
                                                    :credits   (array-map "Margaret Lancaster" ["flute"]
                                                                          "Benjamin Fingland" ["clarinet"]
                                                                          "Miranda Cuckson" ["violin"]
                                                                          "Sumire Kudo" ["cello"]
                                                                          "Steve Beck" ["piano"]
                                                                          "Carl Christian Bettendorf" ["conductor"])}
                                    :offering {:project :personal-space}
                                    :a-weekend-with-the-horse-head {:display-name "The Horse Head"
                                                                    :project      :personal-space})

   :links                (array-map :soundcloud base-sc-url
                                    :spotify "https://player.spotify.com/artist/6FMeK8Rk2ZAATsnr6qd4XP"
                                    :bandcamp base-bc-url
                                    :facebook "https://www.facebook.com/faceofmanband/"
                                    :twitter "https://twitter.com/faceofmanband"
                                    :itunes "https://itunes.apple.com/us/artist/face-of-man/id441404508"
                                    :mr-records "https://mrrecords.co")
   :favorites            [:im-a-flirt :like-devils-fly :ethnopoetics :la-chat-roulette :i-dalliance :a-weekend-with-the-horse-head :lavender]
   :active-panel         nil
   :active-project-id    nil
   :active-collection-id nil
   :playing-track        nil})


(defn ^:private add-track-url [track-id track-data]
  (let [track-name (name track-id)
        [url sc-url score-url ps-bc-url bc-url] (map
                                                  #(str % track-name)
                                                  [base-music-url base-sc-url base-score-url (str base-ps-bc-url "track/") (str base-bc-url "track/")])
        is-ps-project? (= (-> track-data :project) :personal-space)
        has-score? (= (-> track-data :project) :compositions)
        use-sc-url? (or has-score?
                        (= (-> track-data :collection) :covers))
        track-urls (merge {:url (str url ".mp3")}
                          (if is-ps-project?
                            {:bandcamp ps-bc-url}
                            (if use-sc-url?
                              {:soundcloud sc-url}
                              {:bandcamp bc-url}))
                          (if has-score?
                            {:score (str score-url ".pdf")}
                            {}))]
    (merge track-data track-urls)))

(defn ^:private add-track-urls [db]
  (reduce
    (fn [d track-id]
      (update-in d [:tracks track-id] #(add-track-url track-id %)))
    db
    (keys (:tracks base-db))))

;; -- Public API ----------------------------------------------------------
;;
;;
;;

(def default-db
  "When the application first starts, this will be the value assigned to the app-db re-frame atom."
  (add-track-urls base-db))

(defn get-collection-data [collection-id]
  (let [collection-data (collection-id (:collections default-db))
        tracks-for-collection (into [] (filter #(= (-> % val :collection) collection-id) (:tracks default-db)))
        collection-data-with-tracks (assoc collection-data :tracks tracks-for-collection)]
    collection-data-with-tracks))

(defn get-tracks-by-project [project]
  (filter #(= (-> % val :project) project) (:tracks default-db)))

(def links
  (:links default-db))

(def projects
  (distinct (map #(-> % val :project) (-> base-db :tracks))))

(def collections-ids
  (-> base-db :collections keys))


;; -- Local Storage  ----------------------------------------------------------
;;
;; store favorite tracks in local storage
;;

(def lsk "alex-silva-music")                                ; localstore key

(defn ls->favorite-tracks
  "Read in favorite-tracks from LS, and process into a map that can merge into app-db."
  []
  (some->> (.getItem js/localStorage lsk)
           (cljs.reader/read-string)
           (assoc {} :favorites)))

(defn favorite-tracks->ls!
  "Writes favorite tracks into localStorage"
  [db]
  (.setItem js/localStorage lsk (str (:favorites db))))
