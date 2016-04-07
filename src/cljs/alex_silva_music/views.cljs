(ns alex-silva-music.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as str :refer [replace capitalize]]
            [reagent.core :as reagent :refer [atom dom-node current-component]]
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

(defn tracks [is-visible? tracks]
  (let [tracks-margin-top (reagent/atom 40)]
    (reagent/create-class
      {:component-did-mount
       (fn [this]
         (let [this-node (reagent/dom-node this)
               grandparent-node (-> this-node .-parentNode .-parentNode)
               last-uncle-node (-> grandparent-node .-lastChild)
               adjust-tracks-margin-top #(reset!
                                          tracks-margin-top
                                          (- (-> last-uncle-node .getBoundingClientRect .-top)
                                             (-> grandparent-node .getBoundingClientRect .-top)))]
           (adjust-tracks-margin-top)
           (.addEventListener js/window "resize" adjust-tracks-margin-top)))
       :reagent-render
       (fn []
         [:ul.tracks
          {:class (if (is-visible?) "selected" "hidden")
           :style {:margin-top (str @tracks-margin-top "px")}}
          (for [track-data tracks]
            ^{:key (key track-data)}
            [:li [track track-data]])])})))

(defn face-of-man-component [collection-ids]
  (let [active-project-id (subscribe [:active-project-id])
        active-collection-id (subscribe [:active-collection-id])
        top (reagent/atom 360)]
    (reagent/create-class
      {:component-did-mount
       (fn [this]
         (let [this-node (reagent/dom-node this)
               grandparent-node (-> this-node .-parentNode .-parentNode)
               last-uncle-node (-> grandparent-node .-lastChild)
               adjust-top #(reset! top (- (-> last-uncle-node .getBoundingClientRect .-bottom) (-> (.querySelector js/document "html") .getBoundingClientRect .-top)))]
           (adjust-top)
           (.addEventListener js/window "resize" adjust-top)))
       :reagent-render
       (fn []
         [:ul.collections {:class (if (= :face-of-man @active-project-id) "selected" "hidden")
                           :style {:top (str @top "px")}}
          (doall (for [collection-id collection-ids]
                   (let [collection-data (db/get-collection-data collection-id)]
                     [:li.collection {:key collection-id}

                      [:a {:class (if (= collection-id @active-collection-id) "selected")
                           :href  (str "#/projects/face-of-man/" (name collection-id))}
                       (id->name collection-id)]

                      [tracks
                       #(= collection-id @active-collection-id)
                       (:tracks collection-data)]

                      ])))])})))

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
                   ;[music-school-music-component] ; todo
                   )
                 ]
                ))])))

(defn panel [panel-id panel-body]
  (let [active-panel-id (subscribe [:active-panel])]
    (fn []
      [:div.panel {:class (if (= panel-id @active-panel-id) "selected" "hidden")}
       [panel-body]])))

(defn panel-labels [panel-ids]
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
                                   (dispatch [:track-favorite-toggled? false])
                                   " highlight")))
                      :href  (str "#/" (name panel-id))}
                  (id->name panel-id)]
                 ]))])))


(defn track-player []
  (let [playing-track (subscribe [:playing-track])
        toggle-audio-fn (fn [this] (let [track-player (.querySelector (reagent/dom-node this) "audio")]
                                     (if (= :play (:state @playing-track))
                                       (do
                                         (if (:load? @playing-track)
                                           (.load track-player))
                                         (.play track-player))
                                       (.pause track-player))))]
    (reagent/create-class
      {:component-did-mount
       toggle-audio-fn
       :component-did-update
       toggle-audio-fn
       :reagent-render
       (fn []
         [:div.now-playing
          [:audio.controls {:controls "controls"}
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
   [panel-labels db/panels]
   [panel :projects (projects db/projects)]
   [panel :bio (fn [] [:div.bio-text "Alex Silva is dope."])]
   [panel :links links-component]
   [panel :favorites favorites-component]
   [picture]])
