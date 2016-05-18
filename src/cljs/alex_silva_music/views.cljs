(ns alex-silva-music.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as str :refer [replace capitalize]]
            [reagent.core :as reagent :refer [atom dom-node]]
            [alex-silva-music.db :as db]))

;; -- Helper functions ----------------------------------------------------------
;;
;;
;;

(defn capitalize-all [string]
  (str/join " " (map #(if (contains? #{"of" "i" "ii"} %) % (str/capitalize %)) (str/split string #" "))))

(defn id->name [id]
  (-> id name (str/replace "-" " ") capitalize-all))

;; -- Components ----------------------------------------------------------
;;
;;
;;

(defn track-link [track-data link-key]
  [:div {:class    (str (name link-key) " icon")
         :on-click #(.open js/window (-> track-data val link-key) "_blank")}
   [:img {:src (str "/assets/" (name link-key) ".png") :height 30 :width 30}]])

(defn track [track-data]
  (let [track-id (key track-data)
        display-name (-> track-data val :display-name)
        is-favorite (subscribe [:is-favorite track-id])
        playing-track (subscribe [:playing-track])]
    (fn []
      [:a.track {:class
                 (if (= track-id (:track-id @playing-track))
                   (if (= :play (:state @playing-track))
                     "selected playing"
                     "selected"))}
       [:span.track-name {:on-click #(dispatch [:set-playing-track track-id])}
        (if (nil? display-name) (id->name track-id) display-name)]

       (if (-> track-data val :score)
         [track-link track-data :score])

       [:span {:class    (if @is-favorite "favorite" "not-favorite")
               :on-click #(dispatch [:toggle-track-favorited track-id])}
        " ♥"]

       [track-link track-data :soundcloud]

       ])))

(defn resize-top [component top-state]
  (let [this-node (reagent/dom-node component)
        grandparent-node (-> this-node .-parentNode .-parentNode)
        adjust-top #(reset! top-state (-> grandparent-node .getBoundingClientRect .-height))]
    (adjust-top)
    (.addEventListener js/window "resize" adjust-top)))

(defn tracks [is-selected? track-datas]                     ; ignore unused vars warning. see rules about Form-2 components.
  (let [top (reagent/atom 100)]
    (reagent/create-class
      {:component-did-mount
       (fn [this] (resize-top this top))
       :reagent-render
       (fn [is-selected? track-datas]
         [:ul.tracks
          {:class (if (is-selected?) "selected" "hidden")
           :style {:top (str @top "px")}}
          (for [track-data track-datas]
            ^{:key (key track-data)}
            [:li [track track-data]])])})))

(defn face-of-man-component [is-selected?]
  (let [collection-ids db/collections-ids
        active-collection-id (subscribe [:active-collection-id])
        top (reagent/atom 100)]
    (reagent/create-class
      {:component-did-mount
       (fn [this] (resize-top this top))
       :reagent-render
       (fn []
         [:ul.collections {:class (if (is-selected?) "selected" "hidden")
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

(defn projects [is-selected?]
  (let [active-project-id (subscribe [:active-project-id])
        projects-ids db/projects
        top (reagent/atom 100)]
    (reagent/create-class
      {:component-did-mount
       (fn [this] (resize-top this top))
       :reagent-render
       (fn []
         [:ul.projects {:class (if (is-selected?) "selected" "hidden")
                        :style {:top (str @top "px")}}
          (doall (for [project-id projects-ids]
                   (let [is-selected? #(= project-id @active-project-id)]
                     [:li {:key project-id}
                      [:a {:class (if (is-selected?) "selected")
                           :href  (str "#/projects/" (name project-id))}
                       (id->name project-id)]

                      (if (= project-id :face-of-man)
                        [face-of-man-component is-selected?]
                        [tracks is-selected? (db/get-tracks-by-project project-id)])
                      ])
                   ))])})))

(defn links-component [is-selected?]
  (let [links db/links
        top (reagent/atom 100)]
    (reagent/create-class
      {:component-did-mount
       (fn [this] (resize-top this top))
       :reagent-render
       (fn []
         [:ul.links {:class (if (is-selected?) "selected" "hidden")
                     :style {:top (str @top "px")}}
          (for [link links]
            ^{:key (key link)}
            [:li.link
             [:a {:href (val link) :target "_blank"}
              (let [link-name (id->name (key link))]
                [:img {:src (str "/assets/" link-name ".png") :alt link-name :height 90 :width 90}])
              ]])])})))

(defn bio [is-selected?]
  (let [top (reagent/atom 100)]
    (reagent/create-class
      {:component-did-mount
       (fn [this] (resize-top this top))
       :reagent-render
       (fn []
         [:div.bio-text {:class (if (is-selected?) "selected" "hidden")
                         :style {:top (str @top "px")}}
          "Alex Silva is dope."])})))

(defn favorites-component [is-selected?]
  (let [favorite-tracks (subscribe [:favorite-tracks])]
    [tracks is-selected? @favorite-tracks]))

(defn panels [panel-args]
  (let [active-panel (subscribe [:active-panel])
        track-favorite-toggled? (subscribe [:track-favorite-toggled?])]
    (fn []
      [:ul.panels
       (doall (for [panel-arg panel-args]
                (let [[panel-id panel-component] panel-arg]
                  [:li {:key panel-id}
                   [:a {:class (str
                                 (name panel-id)
                                 (if (= panel-id @active-panel) " selected")
                                 (if (and (= :favorites panel-id) @track-favorite-toggled?)
                                   (do
                                     (dispatch [:track-favorite-toggled? false])
                                     " highlight")))
                        :href  (str "#/" (name panel-id))}
                    (id->name panel-id)]

                   [panel-component #(= panel-id @active-panel)]
                   ])))])))

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
             [:span.text (str (if (= :play (:state @playing-track)) "Playing" "Paused") ": ")]
             [:span.text.italic (str "\"" (id->name (:track-id @playing-track)) "\"")]
             ])])})))

(defn picture []
  (let [active-panel (subscribe [:active-panel])]
    [:img.alex {:src   "/assets/alex-studio.png"
                :class (if @active-panel "hidden" "")}]))

(defn main-panel []
  [:div
   [:div.header
    [:h1
     [:a {:href "#/"} "Alex Silva Music"]]
    [track-player]]
   [:hr]
   [panels
    [[:projects projects]
     [:bio bio]
     [:links links-component]
     [:favorites favorites-component]]]
   [picture]])
