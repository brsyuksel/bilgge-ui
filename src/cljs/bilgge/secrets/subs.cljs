(ns bilgge.secrets.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
  ::secrets
  (fn [db _]
    (:secrets db)))

(rf/reg-sub
  ::visibility
  :<- [::secrets]
  (fn [secrets [_ k]]
    (-> secrets :visibility k)))

(rf/reg-sub
  ::data
  :<- [::secrets]
  (fn [secrets _]
    (-> secrets :data)))

(rf/reg-sub
  ::plain
  :<- [::secrets]
  (fn [secrets _]
    (-> secrets :plain)))

(rf/reg-sub
  ::result
  :<- [::secrets]
  (fn [secrets [_ k]]
    (-> secrets :result k)))

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
    (:response response)))
