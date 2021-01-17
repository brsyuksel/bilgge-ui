(ns bilgge.events
  (:require
   [re-frame.core :as re-frame]
   [reitit.frontend.easy :as rfe]
   [bilgge.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

(re-frame/reg-event-db
  ::set-route-name
  (fn-traced [db [_ name]]
             (assoc db :route-name name)))

(re-frame/reg-event-db
  ::set-private-key
  (fn-traced [db [_ priv-key]]
             (assoc db :private-key priv-key)))

(re-frame/reg-event-db
  ::push-state
  (fn-traced [db [_ state]]
             (rfe/push-state state)
             db))
