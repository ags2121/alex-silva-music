(defproject alex-silva-music "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [reagent "0.5.1"]
                 [re-frame "0.6.0"]
                 [secretary "1.2.3"]
                 [figwheel-sidecar "0.5.0"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "script"]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-doo "0.1.6"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"
                                    "resources/public/css/compiled"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src/cljs"]
                        :figwheel     {:on-jsload "alex-silva-music.core/mount-root"}
                        :compiler     {:main                 alex-silva-music.core
                                       :output-to            "resources/public/js/compiled/app.js"
                                       :output-dir           "resources/public/js/compiled/out"
                                       :asset-path           "js/compiled/out"
                                       :source-map-timestamp true}}

                       {:id           "test"
                        :source-paths ["src/cljs" "test/cljs"]
                        :compiler     {:output-to     "resources/public/js/compiled/test.js"
                                       :main          alex-silva-music.runner
                                       :optimizations :none}}

                       {:id           "min"
                        :source-paths ["src/cljs"]
                        :compiler     {:main            alex-silva-music.core
                                       :output-to       "resources/public/js/compiled/app.js"
                                       :optimizations   :advanced
                                       :closure-defines {goog.DEBUG false}
                                       :pretty-print    false}}]})
