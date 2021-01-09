(ns bilgge-ui.login.events
  (:require [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [bilgge-ui.api :as api]))

(defn decrypt-login-cipher
  [_] "")                                                   ;TODO: replace with implementation

(rf/reg-event-db
  ::set-form-data
  (fn [db [_ k v]]
    (assoc-in db [:login :form k] v)))

(rf/reg-event-fx
  ::login-request
  (fn-traced [{:keys [db]} [_ params]]
             {:db (assoc-in db [:login :visibility :loading?] true)
              :http-xhrio (api/login-request params [::login-request-ok] [::login-request-not-ok])}))

(rf/reg-event-db
  ::login-request-not-ok
  (fn-traced [db [_ response]]
             (-> db
                 (assoc-in [:login :visibility :loading?] false)
                 (assoc-in [:login :result :success] false)
                 (assoc-in [:login :result :response] response))))

(rf/reg-event-fx
  ::login-request-ok
  (fn-traced [{:keys [db]} [_ response]]
             (let [cipher (-> response :response :cipher)
                   plain (decrypt-login-cipher cipher)]
               (if-not plain
                 {:db (-> db
                          (assoc-in [:login :visibility :loading?] false)
                          (assoc-in [:login :result :success] false)
                          (assoc-in [:login :result :error :messages] ["decryption failed"]))}
                 {:db (assoc-in db [:login :data :plain] plain)
                  :dispatch [::login-authenticate]}))))

(rf/reg-event-fx
  ::login-authenticate
  (fn-traced [{:keys [db]} [_ params]]
             {:db db
              :http-xhrio (api/login-authenticate params [::login-authenticate-ok] [::login-authenticate-not-ok])}))

(rf/reg-event-db
  ::login-authenticate-not-ok
  (fn-traced [db [_ response]]
             (-> db
                 (assoc-in [:login :visibility :loading?] false)
                 (assoc-in [:login :result :success] false)
                 (assoc-in [:login :result :response] response))))

(rf/reg-event-db
  ::login-authenticate-ok
  (fn-traced [db [_ response]]
             (-> db
                 (assoc-in [:login :visibility :loading?] false)
                 (assoc-in [:login :result :success] true)
                 (assoc :token (-> response :token))
                 (assoc :public-key (-> response :public_key))
                 (assoc :key (-> response :key))
                 (assoc :salt (-> response :salt)))))
