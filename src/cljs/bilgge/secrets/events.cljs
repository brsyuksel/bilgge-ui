(ns bilgge.secrets.events
  (:require [clojure.walk :refer [keywordize-keys]]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [bilgge.api :as api]))

(defn decrypt-secret-info
  [{:keys [type title _iv] :as s} private-key aes-key]
  (-> s
      (assoc :plain-type "")
      (assoc :plain-title "")))

(defn decrypt-secret
  [{:keys [type title content _iv] :as s} private-key aes-key]
  (-> (decrypt-secret-info s private-key aes-key)
      (assoc :plain-content "")))

(rf/reg-event-fx
  ::get-secrets
  (fn-traced [{:keys [db]} [_ params]]
             (let [token (-> db :token)
                   headers {"Authorization" (str "Bearer " token)}]
               {:db (assoc-in db [:secrets :visibility :loading?] true)
                :http-xhrio (api/secrets-list headers params [::get-secrets-ok] [::get-secrets-not-ok])})))

(rf/reg-event-db
  ::get-secrets-not-ok
  (fn-traced [db [_ response]]
             (-> db
                 (assoc-in [:secrets :visibility :loading?] false)
                 (assoc-in [:secrets :result :success] false)
                 (assoc-in [:secrets :result :response :body] (keywordize-keys (:response response)))
                 (assoc-in [:secrets :result :response :status] (:status response)))))

(rf/reg-event-db
  ::get-secrets-ok
  (fn-traced [db [_ response]]
             (let [body (keywordize-keys response)
                   priv-key (:private-key db)
                   aes-key (:key db)
                   decrypt #(decrypt-secret-info % priv-key aes-key)
                   data (:data body)
                   data (map decrypt data)]
               (-> db
                   (assoc-in [:secrets :visibility :loading?] false)
                   (assoc-in [:secrets :result :success] true)
                   (assoc-in [:secrets :data] data)))))

(rf/reg-event-fx
  ::create-secret
  (fn-traced [{:keys [db]} [_ params]]
             (let [token (-> db :token)
                   headers {"Authorization" (str "Bearer " token)}
                   list-params (-> params (select-keys [:collection_id]) (merge {:page 1}))]
               {:db db
                :http-xhrio (api/secrets-create headers params [::create-secret-ok list-params] [::create-secret-not-ok])})))

(rf/reg-event-db
  ::create-secret-not-ok
  (fn-traced [db [_ response]]
             (-> db
                 (assoc-in [:secrets :visibility :loading?] false)
                 (assoc-in [:secrets :result :success] false)
                 (assoc-in [:secrets :result :response :body] (keywordize-keys (:response response)))
                 (assoc-in [:secrets :result :response :status] (:status response)))))

(rf/reg-event-fx
  ::create-secret-ok
  (fn-traced [{:keys [db]} [_ params _]]
             {:db (assoc-in db [:secrets :result :success] true)
              :dispatch [::get-secrets params]}))

(rf/reg-event-fx
  ::get-secret-detail
  (fn-traced [{:keys [db]} [_ id]]
             (let [token (-> db :token)
                   headers {"Authorization" (str "Bearer " token)}]
               {:db (assoc-in db [:secrets :visibility :loading?] true)
                :http-xhrio (api/secret-detail id headers [::get-secret-detail-ok] [::get-secret-detail-not-ok])})))

(rf/reg-event-db
  ::get-secret-detail-not-ok
  (fn-traced [db [_ response]]
             (-> db
                 (assoc-in [:secrets :visibility :loading?] false)
                 (assoc-in [:secrets :result :success] false)
                 (assoc-in [:secrets :result :response :body] (keywordize-keys (:response response)))
                 (assoc-in [:secrets :result :response :status] (:status response)))))

(rf/reg-event-db
  ::get-secret-detail-ok
  (fn-traced [db [_ response]]
             (let [body (keywordize-keys response)
                   priv-key (:private-key db)
                   aes-key (:key db)
                   data (decrypt-secret body priv-key aes-key)]
               (-> db
                   (assoc-in [:secrets :visibility :loading?] false)
                   (assoc-in [:secrets :result :success] true)
                   (assoc-in [:secrets :plain] data)))))

(rf/reg-event-fx
  ::edit-secret
  (fn-traced [{:keys [db]} [_ collection-id id params]]
             (let [token (-> db :token)
                   headers {"Authorization" (str "Bearer " token)}
                   list-params {:offset "0" :limit "10" :collection_id collection-id}]
               {:db db
                :http-xhrio (api/secret-update id headers params [::edit-secret-ok list-params] [::edit-secret-not-ok])})))

(rf/reg-event-db
  ::edit-secret-not-ok
  (fn-traced [db [_ response]]
             (-> db
                 (assoc-in [:secrets :visibility :loading?] false)
                 (assoc-in [:secrets :result :success] false)
                 (assoc-in [:secrets :result :response :body] (keywordize-keys (:response response)))
                 (assoc-in [:secrets :result :response :status] (:status response)))))

(rf/reg-event-fx
  ::edit-secret-ok
  (fn-traced [{:keys [db]} [_ params _]]
             {:db (assoc-in db [:secrets :result :success] true)
              :dispatch [::get-secrets params]}))

(rf/reg-event-fx
  ::delete-secret
  (fn-traced [{:keys [db]} [_ collection-id id]]
             (let [token (-> db :token)
                   headers {"Authorization" (str "Bearer " token)}
                   list-params {:page 1 :collection_id collection-id}]
               {:db db
                :http-xhrio (api/secret-delete id headers [::delete-secret-ok list-params] [::delete-secret-not-ok])})))

(rf/reg-event-db
  ::delete-secret-not-ok
  (fn-traced [db [_ response]]
             (-> db
                 (assoc-in [:secrets :visibility :loading?] false)
                 (assoc-in [:secrets :result :success] false)
                 (assoc-in [:secrets :result :response :body] (keywordize-keys (:response response)))
                 (assoc-in [:secrets :result :response :status] (:status response)))))

(rf/reg-event-fx
  ::delete-secret-ok
  (fn-traced [{:keys [db]} [_ params _]]
             {:db (assoc-in db [:secrets :result :success] true)
              :dispatch [::get-secrets params]}))
