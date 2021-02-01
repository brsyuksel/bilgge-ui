(ns bilgge.login.events
  (:require [clojure.walk :refer [keywordize-keys]]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [bilgge.api :as api]
            [bilgge.utils :as utils]
            [bilgge.events :as e]))

(defn decrypt-login-cipher
  [priv-key cipher-text]
  (utils/decrypt-rsa-string-key priv-key cipher-text))

(rf/reg-event-db
 ::set-data
 (fn [db [_ k v]]
   (assoc-in db [:login :data k] v)))

(rf/reg-event-fx
 ::login-request
 (fn-traced [{:keys [db]} [_ params]]
            {:db (assoc-in db [:login :visibility :loading?] true)
             :http-xhrio (api/login-request params [::login-request-ok params] [::login-request-not-ok])}))

(rf/reg-event-db
 ::login-request-not-ok
 (fn-traced [db [_ response]]
            (-> db
                (assoc-in [:login :visibility :loading?] false)
                (assoc-in [:login :result :success] false)
                (assoc-in [:login :result :response :body] (keywordize-keys (:response response)))
                (assoc-in [:login :result :response :status] (:status response)))))

(rf/reg-event-fx
 ::login-request-ok
 (fn-traced [{:keys [db]} [_ params response]]
            (let [body (keywordize-keys response)
                  priv-key (:private-key db)
                  cipher (:cipher body)
                  plain (decrypt-login-cipher priv-key cipher)
                  params (merge params {:plain plain})]
              (if-not plain
                {:db (-> db
                         (assoc-in [:login :visibility :loading?] false)
                         (assoc-in [:login :result :success] false)
                         (assoc-in [:login :result :error :messages] ["decryption failed"]))}
                {:db (assoc-in db [:login :data :plain] plain)
                 :dispatch [::login-authenticate params]}))))

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
                (assoc-in [:login :result :response :body] (keywordize-keys (:response response)))
                (assoc-in [:login :result :response :status] (:status response)))))

(rf/reg-event-fx
 ::login-authenticate-ok
 (fn-traced [{:keys [db]} [_ response]]
            (let [body (keywordize-keys response)]
              {:db (-> db
                       (assoc-in [:login :visibility :loading?] false)
                       (assoc-in [:login :result :success] true)
                       (assoc :token (-> body :token))
                       (assoc :public-key (-> body :public_key))
                       (assoc :key (-> body :key))
                       (assoc :plain-key (decrypt-login-cipher (:private-key db) (:key body)))
                       (assoc :salt (-> body :salt))
                       (assoc :plain-salt (decrypt-login-cipher (:private-key db) (:salt body))))
               :dispatch [::e/push-state :app-page]})))
