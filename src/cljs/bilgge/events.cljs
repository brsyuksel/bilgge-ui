(ns bilgge.events
  (:require [clojure.walk :refer [keywordize-keys]]
            [clojure.string :refer [join]]
            [re-frame.core :as re-frame]
            [reitit.frontend.easy :as rfe]
            ["bulma-toast" :as bulma-toast]
            [bilgge.db :as db]
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
            db/default-db))

(re-frame/reg-event-db
 ::display-warning-message
 (fn-traced [_ [_ message]]
            (bulma-toast/toast #js {:message message
                                    :type "is-warning"
                                    :pauseOnHover true})))

(re-frame/reg-event-db
 ::display-danger-message
 (fn-traced [_ [_ message]]
            (bulma-toast/toast #js {:message message
                                    :type "is-danger"
                                    :pauseOnHover true})))

(re-frame/reg-event-fx
 ::display-response-errors
 (fn-traced [_ [_ response]]
            (let [status (:status response)
                  body (keywordize-keys (:response response))
                  fx (case status
                       403 ::display-danger-message
                       500 ::display-danger-message
                       ::display-warning-message)
                  msg (case status
                        403 "Authentication error. Try to log out and then log in again."
                        500 "Internal error."
                        (join "," (:messages body)))]
              (when msg {:dispatch [fx msg]}))))

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
