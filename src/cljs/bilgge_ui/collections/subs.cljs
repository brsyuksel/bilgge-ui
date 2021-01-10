(ns bilgge-ui.collections.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
  ::collections
  (fn [db _]
    (:collections db)))

(rf/reg-sub
  ::visibility
  :<- [::collections]
  (fn [collections [_ k]]
    (-> collections :visibility k)))

(rf/reg-sub
  ::data
  :<- [::collections]
  (fn [collections _]
    (-> collections :data)))

(rf/reg-sub
  ::result
  :<- [::collections]
  (fn [collections [_ k]]
    (-> collections :result k)))

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