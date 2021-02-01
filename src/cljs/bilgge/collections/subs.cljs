(ns bilgge.collections.subs
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
   (:data collections)))

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
   (:body response)))

(rf/reg-sub
 ::response-status
 :<- [::response]
 (fn [response _]
   (:status response)))
