(ns alex-silva-music.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as str :refer [replace capitalize]]
            [reagent.core :as reagent :refer [dom-node]]
            [alex-silva-music.db :as db]))

(defn capitalize-all [string]
  (str/join " " (map #(if (contains? #{"of" "i" "ii"} %) % (str/capitalize %)) (str/split string #" "))))

(defn id->name [id]
  (-> id name (str/replace "-" " ") capitalize-all))

(defn track-link [track-data link-key]
  [:div {:class (str (name link-key) " icon")
         :on-click #(.open js/window (-> track-data val link-key) "_blank")}
   [:img {:src (str "/assets/" (name link-key) ".png") :height 30 :width 30}]])

(defn track [track-data]
  (let [track-id (key track-data)
        display-name (-> track-data val :display-name)
        is-liked (subscribe [:is-liked track-id])
        playing-track (subscribe [:playing-track])]
    (fn []
      [:a.track {:class    (if (= track-id (:track-id @playing-track)) "selected")}
       [:span.track-name {:on-click #(dispatch [:set-playing-track track-id])}
        (if (nil? display-name) (id->name track-id) display-name)]

       (if (-> track-data val :score)
         [track-link track-data :score] )

       [:span {:class    (if @is-liked "liked" "not-liked")
               :on-click #(dispatch [:toggle-track-favorited track-id])}
        " ♥"]

       [track-link track-data :soundcloud]

       ])))

(defn panel [panel-id panel-body]
  (let [active-panel-id (subscribe [:active-panel])]
    (fn []
      [:div.panel {:class (if (= panel-id @active-panel-id) "selected" "hidden")}
       [panel-body]])))

(defn face-of-man-component [collection-ids]
  (let [active-project-id (subscribe [:active-project-id])
        active-collection-id (subscribe [:active-collection-id])]
    (fn []
      [:ul.collections {:class (if (= :face-of-man @active-project-id) "selected" "hidden")}
       (doall (for [collection-id collection-ids]
          (let [collection-data (db/get-collection-data collection-id)]
            [:li.collection {:key collection-id}

            [:a {:class (if (= collection-id @active-collection-id) "selected")
                 :href  (str "#/projects/face-of-man/" (name collection-id))}
             ;(str (id->name collection-id) (if-not (nil? (:year @collection)) (str " (" (:year @collection) ")"))) ;todo: add year?
             (id->name collection-id)
             ]

            [:ul.tracks {:class (if (= collection-id @active-collection-id) "selected" "hidden")}
             (for [track-data (:tracks collection-data)]
               ^{:key (key track-data)}
               [:li [track track-data]])]

            ])))])))

(defn music-school-music-component []
  (let [other-tracks (subscribe [:tracks-by-project :compositions])]
    (fn []
      [:ul.other
       (for [track-data @other-tracks]
         ^{:key (key track-data)}
         [:li.track-container
          [track track-data]])])))

(defn links-component []
  (let [links (subscribe [:links])]
    (fn []
      [:ul.links
       (for [link @links]
         ^{:key (key link)}
         [:li.link
          [:a {:href (val link) :target "_blank"}
           (let [link-name (id->name (key link))]
             [:img {:src (str "/assets/" link-name ".png") :alt link-name :height 90 :width 90}])
           ]])])))

(defn favorites-component []
  (let [liked-tracks (subscribe [:liked-tracks])]
    (fn []
      [:ul.tracks
       (for [track-data @liked-tracks]
         ^{:key (key track-data)}
         [:li [track track-data]])]
      )))

(defn projects [projects-ids]
  (let [active-project-id (subscribe [:active-project-id])]
    (fn []
      [:ul.projects
       (doall (for [project-id projects-ids]
                ^{:key project-id}
                [:li
                 [:a {:class (if (= project-id @active-project-id) "selected")
                      :href  (str "#/projects/" (name project-id))}
                  (id->name project-id)]
                 (if (= project-id :face-of-man)
                   [face-of-man-component db/collections-ids]
                   ;[music-school-music-component] ;todo
                   )
                 ]
                ))])))

(defn schedule-remove-highlight []
  (.setTimeout js/window
               (fn []
                 (-> (.getElementsByClassName js/document "favorites")
                    (.item 0)
                    .-classList
                    (.remove "highlight"))
                 (dispatch [:track-favorite-toggled? false]))
               1))

(defn panels [panel-ids]
  (let [active-panel (subscribe [:active-panel])
        track-favorite-toggled? (subscribe [:track-favorite-toggled?])]
    (fn []
      [:ul.panels
       (doall (for [panel-id panel-ids]
          ^{:key panel-id}
          [:li

           [:a {:class (str
                         (name panel-id)
                         (if (= panel-id @active-panel) " selected")
                         (if (and (= :favorites panel-id) @track-favorite-toggled?)
                           (do
                             (schedule-remove-highlight)
                             " highlight")))

                :href  (str "#/" (name panel-id))}


            (id->name panel-id)
            ]

           ]))])))


(defn track-player []
  (let [playing-track (subscribe [:playing-track])
        toggle-audio-fn #(let [track-player (.item (.getElementsByTagName js/document "audio") 0)]
                          (if (= :play (:state @playing-track))
                            (do
                              (if (:load? @playing-track)
                                (.load track-player))
                              (.play track-player))
                            (.pause track-player)))]
    (reagent/create-class
      {:component-did-mount
       toggle-audio-fn
       :component-did-update
       toggle-audio-fn
       :reagent-render
       (fn []
         [:div.now-playing
          [:audio.controls.hidden {:controls "controls"}
           [:source {:src      (if @playing-track (:url @playing-track))
                     :type     "audio/mpeg"
                     :controls "controls"}]]

          (if @playing-track
            [:div
             [:span.text "Now Playing: "]
             [:span.text.italic (str "\"" (id->name (:track-id @playing-track)) "\"")]
             ])])})))

(defn picture []
  (let [active-panel (subscribe [:active-panel])]
    [:img.alex {:src "/assets/alex-studio.png"
                :class (if @active-panel "hidden" "")}]))

(defn main-panel []
  [:div
   [:div.header
    [:h1
     [:a {:href "#/"} "alex silva music"]]
    [track-player]]
   [:hr]
   [panels db/panels]
   [panel :projects (projects db/projects)]
   [panel :bio (fn [] [:div.bio-text "Alex Silva is dope."])]
   [panel :links links-component]
   [panel :favorites favorites-component]
   [picture]])
