(ns bilgge.register.events
  (:require [clojure.walk :refer [keywordize-keys]]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [bilgge.api :as api]))

(rf/reg-event-fx
  ::register
  (fn-traced [{:keys [db]} [_ params]]
             {:db (assoc-in db [:register :visibility :loading?] true)
              :http-xhrio (api/register params [::register-ok] [::register-not-ok])}))

(rf/reg-event-db
  ::register-ok
  (fn-traced [db _]
             (-> db
                 (assoc-in [:register :visibility :loading?] false)
                 (assoc-in [:register :result :success] true))))

(rf/reg-event-db
  ::register-not-ok
  (fn-traced [db [_ response]]
             (-> db
                 (assoc-in [:register :visibility :loading?] false)
                 (assoc-in [:register :result :success] false)
                 (assoc-in [:register :result :response :body] (keywordize-keys (:response response)))
                 (assoc-in [:register :result :response :status] (:status response)))))

(rf/reg-event-db
  ::set-error-message
  (fn-traced [db [_ message]]
             (assoc-in db [:register :result :error :messages] [message])))
