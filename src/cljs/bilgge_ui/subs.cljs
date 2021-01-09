(ns bilgge-ui.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::token
 (fn [db]
   (:token db)))
