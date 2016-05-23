(defproject alex-silva-music "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [bk/ring-gzip "0.1.1"]
                 [ring.middleware.logger "0.5.0"]
                 [compojure "1.4.0"]
                 [environ "1.0.2"]
                 [reagent "0.5.1"]
                 [re-frame "0.6.0" :exclusions [org.clojure/clojurescript]]
                 [secretary "1.2.3"]
                 [prismatic/schema "1.1.1"]]

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-environ "1.0.1"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs" "script"]

  :test-paths ["test/clj"]

  :clean-targets ^{:protect false} [:target-path :compile-path "resources/public/js"]

  :uberjar-name "alex-silva-music.jar"

  ;; Use `lein run` if you just want to start a HTTP server, without figwheel
  :main alex-silva-music.server

  ;:repl-options {:init-ns user}

  :cljsbuild {:builds
              {:app
               {:source-paths ["src/cljs"]

                :figwheel     true
                ;; Alternatively, you can configure a function to run every time figwheel reloads.
                ; :figwheel {:on-jsload "alex-silva-music.core/mount-root"}


                :compiler     {:main                 alex-silva-music.core
                               :asset-path           "js/compiled/out"
                               :output-to            "resources/public/js/compiled/app.js"
                               :output-dir           "resources/public/js/compiled/out"
                               :source-map-timestamp true}}}}

  ;; When running figwheel from nREPL, figwheel will read this configuration
  ;; stanza, but it will read it without passing through leiningen's profile
  ;; merging. So don't put a :figwheel section under the :dev profile, it will
  ;; not be picked up, instead configure figwheel here on the top level.

  :figwheel {;; :http-server-root "public"       ;; serve static assets from resources/public/
             ;; :server-port 3449                ;; default
             ;; :server-ip "127.0.0.1"           ;; default
             :css-dirs       ["resources/public/css"]       ;; watch and update CSS

             ;; Instead of booting a separate server on its own port, we embed
             ;; the server ring handler inside figwheel's http-kit server, so
             ;; assets and API endpoints can all be accessed on the same host
             ;; and port. If you prefer a separate server process then take this
             ;; out and start the server with `lein run`.
             :ring-handler   alex-silva-music.server/http-handler

             ;; Start an nREPL server into the running figwheel process. We
             ;; don't do this, instead we do the opposite, running figwheel from
             ;; an nREPL process, see
             ;; https://github.com/bhauman/lein-figwheel/wiki/Using-the-Figwheel-REPL-within-NRepl
             ;; :nrepl-port 7888

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             :server-logfile "log/figwheel.log"}

  :doo {:build "test"}

  :profiles {:dev
             {:dependencies [[figwheel-sidecar "0.5.0" :exclusions [org.clojure/clojurescript]]
                             [com.cemerick/piggieback "0.2.1"]
                             [org.clojure/tools.nrepl "0.2.12"]]

              :plugins      [[lein-doo "0.1.6"]]

              :cljsbuild    {:builds
                             {:test
                              {:source-paths ["src/cljs" "test/cljs"]
                               :compiler
                                             {:output-to     "resources/public/js/compiled/testable.js"
                                              :main          alex-silva-music.runner
                                              :optimizations :none
                                              :pretty-print  true}}}}}

             :uberjar
             {:source-paths ^:replace ["src/clj"]
              :hooks        [leiningen.cljsbuild]
              :omit-source  true
              :aot          :all
              :cljsbuild    {:builds
                             {:app
                              {:source-paths ^:replace ["src/cljs"]
                               :compiler
                                             {:optimizations :advanced
                                              :pretty-print  false}}}}}})
