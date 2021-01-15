(ns bilgge.register.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
  ::register
  (fn [db _]
    (:register db)))

(rf/reg-sub
  ::visibility
  :<- [::register]
  (fn [register [_ k]]
    (-> register :visibility k)))

(rf/reg-sub
  ::loading?
  :<- [::visibility :loading?]
  (fn [loading? _]
    loading?))

(rf/reg-sub
  ::result
  :<- [::register]
  (fn [register [_ k]]
    (-> register :result k)))

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