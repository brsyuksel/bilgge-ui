(ns bilgge.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::token
 (fn [db]
   (:token db)))

(re-frame/reg-sub
  ::private-key
  (fn [db]
      (:private-key db)))

(re-frame/reg-sub
  ::public-key
  (fn [db]
      (:public-key db)))

(re-frame/reg-sub
  ::plain-key
  (fn [db]
      (:plain-key db)))

(re-frame/reg-sub
  ::plain-salt
  (fn [db]
      (:plain-salt db)))

(re-frame/reg-sub
  ::route-name
  (fn [db]
      (:route-name db)))
