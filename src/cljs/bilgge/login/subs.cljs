(ns bilgge.login.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::login
 (fn [db _]
   (:login db)))

(rf/reg-sub
 ::visibility
 :<- [::login]
 (fn [login [_ k]]
   (-> login :visibility k)))

(rf/reg-sub
 ::data
 :<- [::login]
 (fn [login [_ k]]
   (-> login :data k)))

(rf/reg-sub
 ::result
 :<- [::login]
 (fn [login [_ k]]
   (-> login :result k)))

(rf/reg-sub
 ::success?
 :<- [::result :success]
 (fn [success? _]
   success?))

(rf/reg-sub
 ::response
 :<- [::result :response]
 (fn [response _]
   response))

(rf/reg-sub
 ::response-body
 :<- [::response]
 (fn [response _]
   (:body response)))

(rf/reg-sub
 ::response-status
 :<- [::response]
 (fn [response _]
   (:status response)))

(rf/reg-sub
 ::body-messages
 :<- [::response-body]
 (fn [body _]
   (:messages body)))

(rf/reg-sub
 ::error-messages
 :<- [::result :error]
 (fn [err _]
   (:messages err)))
