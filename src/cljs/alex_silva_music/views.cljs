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

(defn track-link [track-url link-key]
  [:div {:class    (str (name link-key) " icon")
         :on-click #(.open js/window track-url "_blank")}
   [:img {:src (str "/assets/" (name link-key) ".png") :height 30 :width 30}]])

(defn track [track-data]
  (let [track-id (key track-data)
        track-data (val track-data)
        display-name (:display-name track-data)
        is-favorite (subscribe [:is-favorite track-id])
        playing-track (subscribe [:playing-track])]
    (fn []
      [:a.track {:class
                 (if (= track-id (:track-id @playing-track))
                   (if (= :play (:state @playing-track))
                     "selected playing"
                     "selected"))}
       [:span.track-name {:on-click #(dispatch [:set-playing-track track-id]) :value (:url track-data)}
        (if (nil? display-name) (id->name track-id) display-name)]

       (if (:score track-data)
         [track-link (:score track-data) :score])

       [:span {:class    (if @is-favorite "favorite" "not-favorite")
               :on-click #(dispatch [:toggle-track-favorited track-id])}
        " ♥"]

       [track-link
        (if (= :personal-space (:project track-data))
          (:soundcloud-ps track-data)
          (:soundcloud track-data))
        :soundcloud]])))

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
                       (:tracks collection-data)]])))])})))

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
                        [tracks is-selected? (db/get-tracks-by-project project-id)])])))])})))

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
              [:img {:src    (str "/assets/" (name (key link)) ".png")
                     :alt    (id->name (key link))
                     :height 90
                     :width  90}]]])])})))

(defn bio [is-selected?]
  (let [top (reagent/atom 100)]
    (reagent/create-class
      {:component-did-mount
       (fn [this]
         (resize-top this top)
         )
       :reagent-render
       (fn []
         [:div.bio-text {:class (if (is-selected?) "selected" "hidden")
                         :style {:top (str @top "px")}}
          [:div "Alex Silva is " [:br.rwd-break2] "a Brooklyn-based " [:br.rwd-break] "musician."]
          [:div "He records and performs " [:br.rwd-break2] "as Face of Man "]
          [:div "and is also a member of " [:br.rwd-break2] "the band " [:a {:href "http://personalspacetheband.com" :target "_blank"} "Personal Space"] "."]
          [:br]
          [:div "Alex Silva 88 {At} " [:br.rwd-break2] "Gmail {Dot} Com"]
          [:div {:class           "fb-like"
                 :data-href       "https://www.facebook.com/faceofmanband/"
                 :data-width      "200"
                 :data-layout     "standard"
                 :data-action     "like"
                 :data-show-faces "false"
                 :data-share      "false"}]])})))

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
                                     (dispatch [:reset-track-favorite-toggled])
                                     " highlight")))
                        :href  (str "#/" (name panel-id))}
                    (id->name panel-id)]

                   [panel-component #(= panel-id @active-panel)]])))])))

(defn track-player []
  (let [playing-track (subscribe [:playing-track])
        toggle-audio-fn (fn [this] (let [track-player (.querySelector (reagent/dom-node this) "audio")]
                                     (if (= :play (:state @playing-track))
                                       (do
                                         (if (:load? @playing-track)
                                           (.load track-player))
                                         (.play track-player))
                                       (.pause track-player))))] ; for toggling audio using reframe event system
    (reagent/create-class
      {:component-did-mount
       (fn [this]
         ;(toggle-audio-fn this)
         (let [track-player (.querySelector (reagent/dom-node this) "audio")]
           (.addEventListener js/window "keyup" (fn [event]
                                                  (if (= 32 (.-keyCode event))
                                                    (do
                                                      (dispatch [:toggle-playing-track-state])
                                                      (if (.-paused track-player)
                                                        (.play track-player)
                                                        (.pause track-player))))))

           (.addEventListener js/window "keydown" (fn [event]
                                                    (if (= 32 (.-keyCode event))
                                                      (.preventDefault event)))) ; stop spacebar from scrolling

           ;; mobile browsers will only allow direct user actions to trigger audio load
           ;; so we have to bypass reframe for now in order to satisfy that :(
           (let [track-components (.querySelectorAll js/document "span.track-name")]
             (dotimes [i (.-length track-components)]
               (.addEventListener (.item track-components i) "click" (fn [e]
                                                                       (let [audio-source (.querySelector track-player "source")
                                                                             clicked-track-url (-> e .-target (.getAttribute "value"))
                                                                             clicked-same-track? (and (not (.-ended track-player))
                                                                                                      (< 0 (.-currentTime track-player))
                                                                                                      (= clicked-track-url (.getAttribute audio-source "src")))]
                                                                         (.preventDefault e)
                                                                         (if clicked-same-track?
                                                                           (if (.-paused track-player)
                                                                             (.play track-player)
                                                                             (.pause track-player))
                                                                           (do
                                                                             (.setAttribute audio-source "src" clicked-track-url)
                                                                             (.pause track-player)
                                                                             (.load track-player)
                                                                             (aset track-player "oncanplaythrough" (.play track-player)))))))))))

       ;:component-did-update
       ;toggle-audio-fn
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
             [:span.text.italic (str "\"" (:display-name @playing-track) "\"")]])])})))

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
