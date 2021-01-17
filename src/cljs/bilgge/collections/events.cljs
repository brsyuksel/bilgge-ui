(ns bilgge.collections.events
  (:require [clojure.walk :refer [keywordize-keys]]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [bilgge.api :as api]))

(defn decrypt-collection-name
  [{:keys [name _iv] :as d} private-key aes-key]
  (assoc d :plain-name ""))

(rf/reg-event-fx
  ::get-collections
  (fn-traced [{:keys [db]} _]
             (let [token (-> db :token)
                   headers {"Authorization" (str "Bearer " token)}]
               {:db (assoc-in db [:collections :visibility :loading?] true)
                :http-xhrio (api/collections-list headers nil [::get-collections-ok] [::get-collections-not-ok])})))

(rf/reg-event-db
  ::get-collections-not-ok
  (fn-traced [db [_ response]]
             (-> db
                 (assoc-in [:collections :visibility :loading?] false)
                 (assoc-in [:collections :result :success] false)
                 (assoc-in [:collections :result :response :body] (keywordize-keys (:response response)))
                 (assoc-in [:collections :result :response :status] (:status response)))))

(rf/reg-event-db
  ::get-collections-ok
  (fn-traced [db [_ response]]
             (let [body (keywordize-keys response)
                   priv-key (:private-key db)
                   aes-key (:key db)
                   decrypt #(decrypt-collection-name % priv-key aes-key)
                   data (:data body)
                   data (map decrypt data)]
               (-> db
                   (assoc-in [:collections :visibility :loading?] false)
                   (assoc-in [:collections :result :success] true)
                   (assoc-in [:collections :data] data)))))

(rf/reg-event-fx
  ::create-collection
  (fn-traced [{:keys [db]} [_ params]]
             (let [token (-> db :token)
                   headers {"Authorization" (str "Bearer " token)}]
               {:db db
                :http-xhrio (api/collections-create headers params [::create-collection-ok] [::create-collection-not-ok])})))

(rf/reg-event-db
  ::create-collection-not-ok
  (fn-traced [db [_ response]]
             (-> db
                 (assoc-in [:collections :visibility :loading?] false)
                 (assoc-in [:collections :result :success] false)
                 (assoc-in [:collections :result :response :body] (keywordize-keys (:response response)))
                 (assoc-in [:collections :result :response :status] (:status response)))))

(rf/reg-event-fx
  ::create-collection-ok
  (fn-traced [{:keys [db]} _]
             {:db (assoc-in db [:collections :result :success] true)
              :dispatch [::get-collections]}))

(rf/reg-event-fx
  ::edit-collection
  (fn-traced [{:keys [db]} [_ id params]]
             (let [token (-> db :token)
                   headers {"Authorization" (str "Bearer " token)}]
               {:db db
                :http-xhrio (api/collection-update id headers params [::edit-collection-ok] [::edit-collection-not-ok])})))

(rf/reg-event-db
  ::edit-collection-not-ok
  (fn-traced [db [_ response]]
             (-> db
                 (assoc-in [:collections :visibility :loading?] false)
                 (assoc-in [:collections :result :success] false)
                 (assoc-in [:collections :result :response :body] (keywordize-keys (:response response)))
                 (assoc-in [:collections :result :response :status] (:status response)))))

(rf/reg-event-fx
  ::edit-collection-ok
  (fn-traced [{:keys [db]} _]
             {:db (assoc-in db [:collections :result :success] true)
              :dispatch [::get-collections]}))

(rf/reg-event-fx
  ::delete-collection
  (fn-traced [{:keys [db]} [_ id]]
             (let [token (-> db :token)
                   headers {"Authorization" (str "Bearer " token)}]
               {:db db
                :http-xhrio (api/collection-delete id headers [::delete-collection-ok] [::delete-collection-not-ok])})))

(rf/reg-event-db
  ::delete-collection-not-ok
  (fn-traced [db [_ response]]
             (-> db
                 (assoc-in [:collections :visibility :loading?] false)
                 (assoc-in [:collections :result :success] false)
                 (assoc-in [:collections :result :response :body] (keywordize-keys (:response response)))
                 (assoc-in [:collections :result :response :status] (:status response)))))

(rf/reg-event-fx
  ::delete-collection-ok
  (fn-traced [{:keys [db]} _]
             {:db (assoc-in db [:collections :result :success] true)
              :dispatch [::get-collections]}))