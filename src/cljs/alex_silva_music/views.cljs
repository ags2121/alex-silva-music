(ns alex-silva-music.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as str :refer [replace capitalize]]
            [reagent.core :as reagent :refer [dom-node]]
            [alex-silva-music.db :as db]))

(defn capitalize-all [string]
  (str/join " " (map #(if (contains? #{"of" "i" "ii"} %) % (str/capitalize %)) (str/split string #" "))))

(defn id->name [id]
  (-> id name (str/replace "-" " ") capitalize-all))

(defn track [track-data]
  (let [track-id (key track-data)
        display-name (-> track-data val :display-name)
        is-liked (subscribe [:is-liked track-id])
        active-track-id (subscribe [:active-track-id])
        is-active-track (= track-id @active-track-id)]
    (fn []
      [:div.track
       [:span {:class    (if is-active-track "selected")
               :on-click #(dispatch [:set-playing-track track-id])}
        (if (nil? display-name) (id->name track-id) display-name)]
       [:span {:class    (if @is-liked "liked" "not-liked")
               :on-click #(dispatch [:set-track-liked track-id])} " ♥"]
       ])))

(defn collection [panel-id collection-id]
  (let [active-collection-id (subscribe [:active-collection-id])
        collection (subscribe [:collection collection-id])]
    (fn []
      [:div.collection
       [:a {:href (str "#/" (name panel-id) "/" (name collection-id))}
        (str (id->name collection-id) (if-not (nil? (:year @collection)) (str " (" (:year @collection) ")")))]
       [:ul {:class (if (= collection-id @active-collection-id) "selected" "hidden")}
        (for [track-data (:tracks @collection)]
          ^{:key (key track-data)}
          [:li [track track-data]])]]
      )))

(defn panel [panel-id panel-body]
  (let [active-panel-id (subscribe [:active-panel])]
    (fn []
      [:div {:class (if (= panel-id @active-panel-id) "selected" "hidden")}
       [:h1 (id->name panel-id)]
       [panel-body]])))

(defn face-of-man-component []
  (let [collections (subscribe [:collections])]
    (fn []
      [:ul.collections
       (for [collection-id @collections]
         ^{:key collection-id}
         [:li [collection :face-of-man collection-id]])])))

(defn other-component []
  (let [other-tracks (subscribe [:tracks-by-category :other])]
    (fn []
      [:ul.other
       (for [track-data @other-tracks]
         ^{:key (key track-data)}
         [:li.track
          [track track-data]])])))

(defn links-component []
  (let [links (subscribe [:links])]
    (fn []
      [:ul.links
       (for [link @links]
         ^{:key (key link)}
         [:li.link
          [:a {:href (val link) :target "_blank"} (id->name (key link))]])])))

(defn likes-component []
  (let [liked-tracks (subscribe [:liked-tracks])]
    (fn []
      [:ul.liked-tracks
       (for [track-data @liked-tracks]
         ^{:key (key track-data)}
         [:li [track track-data]])]
      )))

(defn header []
  (let [header-data (subscribe [:header-data])]
    (fn []
      (let [[liked-tracks-count panels active-panel-id] @header-data]
        [:ul.panels
         (for [panel-id panels]
           ^{:key panel-id}
           [:li
            [:a {:class (if (= panel-id active-panel-id) "selected")
                 :href  (str "#/" (name panel-id))}
             (id->name panel-id)
             (if (= panel-id :likes)
               (str " (" liked-tracks-count ")"))]
            ])
         ]))))

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
         [:div
          [:audio {:controls "controls"}
           [:source {:src      (if-not (nil? @playing-track) (db/get-track-link (name (:track-id @playing-track))))
                     :type     "audio/mpeg"
                     :controls "controls"}]]
          [:div.now-playing
           "Now Playing: "
           [:span.italic (str "\"" (id->name (:track-id @playing-track)) "\"")]
           ]])})))

(defn main-panel []
  [:div
   [track-player]
   [header]
   [panel :face-of-man face-of-man-component]
   [panel :other other-component]
   [panel :links links-component]
   [panel :likes likes-component]])
