(ns alex-silva-music.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [alex-silva-music.core-test]))

(doo-tests 'alex-silva-music.core-test)
