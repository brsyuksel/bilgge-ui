(ns bilgge.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::token
 (fn [db]
   (:token db)))

(re-frame/reg-sub
  ::route-name
  (fn [db]
      (:route-name db)))
