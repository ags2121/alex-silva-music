(ns alex-silva-music.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [alex-silva-music.handlers-test]))

;; See README.md for instructions on how to run tests
(doo-tests 'alex-silva-music.handlers-test)
