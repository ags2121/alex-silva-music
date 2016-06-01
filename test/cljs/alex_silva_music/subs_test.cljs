(ns alex-silva-music.subs-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [alex-silva-music.subs :as s]
            [alex-silva-music.db :as db]
            [schema.test :as st]))

(use-fixtures :once st/validate-schemas)

