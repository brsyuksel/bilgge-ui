(ns bilgge.secrets.events
  (:require [clojure.walk :refer [keywordize-keys]]
            [clojure.string :as string]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [bilgge.api :as api]
            [bilgge.events :as events]))

(rf/reg-event-fx
 ::get-secrets
 (fn-traced [{:keys [db]} [_ params]]
            (let [token (:token db)
                  headers {"Authorization" (str "Bearer " token)}
                  hashes (-> db :secrets :search-hashes)
                  q (if hashes (string/join "," hashes) "")
                  params (merge params {:q q})]
              {:db (-> db
                       (assoc-in [:secrets :visibility :loading?] true)
                       (assoc-in [:secrets :visibility :list-loading?] true))
               :http-xhrio (api/secrets-list headers params [::get-secrets-ok] [::get-secrets-not-ok])})))

(rf/reg-event-fx
 ::get-secrets-not-ok
 (fn-traced [{:keys [db]} [_ response]]
            {:db (-> db
                     (assoc-in [:secrets :visibility :loading?] false)
                     (assoc-in [:secrets :visibility :list-loading?] false)
                     (assoc-in [:secrets :result :success] false)
                     (assoc-in [:secrets :result :response :body] (keywordize-keys (:response response)))
                     (assoc-in [:secrets :result :response :status] (:status response)))
             :dispatch [::events/display-response-errors response]}))

(rf/reg-event-db
 ::get-secrets-ok
 (fn-traced [db [_ response]]
            (let [body (keywordize-keys response)
                  data (:data body)]
              (-> db
                  (assoc-in [:secrets :visibility :loading?] false)
                  (assoc-in [:secrets :visibility :list-loading?] false)
                  (assoc-in [:secrets :result :success] true)
                  (assoc-in [:secrets :data] data)))))

(rf/reg-event-fx
 ::create-secret
 (fn-traced [{:keys [db]} [_ params]]
            (let [token (:token db)
                  headers {"Authorization" (str "Bearer " token)}
                  list-params (-> params (select-keys [:collection_id]) (merge {:offset "0" :limit "10"}))]
              {:db db
               :http-xhrio (api/secrets-create headers params [::create-secret-ok list-params] [::create-secret-not-ok])})))

(rf/reg-event-fx
 ::create-secret-not-ok
 (fn-traced [{:keys [db]} [_ response]]
            {:db (-> db
                     (assoc-in [:secrets :visibility :loading?] false)
                     (assoc-in [:secrets :result :success] false)
                     (assoc-in [:secrets :result :response :body] (keywordize-keys (:response response)))
                     (assoc-in [:secrets :result :response :status] (:status response)))
             :dispatch [::events/display-response-errors response]}))

(rf/reg-event-fx
 ::create-secret-ok
 (fn-traced [{:keys [db]} [_ params _]]
            {:db (-> db
                     (assoc-in [:secrets :result :success] true)
                     (assoc-in [:secrets :visibility :display-editor?] nil)
                     (assoc-in [:secrets :selected-id] nil)
                     (assoc-in [:secrets :selected-secret-type] nil)
                     (assoc-in [:secrets :detail] nil))
             :dispatch [::get-secrets params]}))

(rf/reg-event-fx
 ::get-secret-detail
 (fn-traced [{:keys [db]} [_ id]]
            (let [token (:token db)
                  headers {"Authorization" (str "Bearer " token)}]
              {:db (assoc-in db [:secrets :visibility :detail-loading?] true)
               :http-xhrio (api/secret-detail id headers [::get-secret-detail-ok] [::get-secret-detail-not-ok])})))

(rf/reg-event-fx
 ::get-secret-detail-not-ok
 (fn-traced [{:keys [db]} [_ response]]
            {:db (-> db
                     (assoc-in [:secrets :visibility :detail-loading?] false)
                     (assoc-in [:secrets :result :success] false)
                     (assoc-in [:secrets :result :response :body] (keywordize-keys (:response response)))
                     (assoc-in [:secrets :result :response :status] (:status response)))
             :dispatch [::events/display-response-errors response]}))

(rf/reg-event-db
 ::get-secret-detail-ok
 (fn-traced [db [_ response]]
            (let [body (keywordize-keys response)]
              (-> db
                  (assoc-in [:secrets :visibility :detail-loading?] false)
                  (assoc-in [:secrets :result :success] true)
                  (assoc-in [:secrets :detail] body)))))

(rf/reg-event-fx
 ::edit-secret
 (fn-traced [{:keys [db]} [_ collection-id id params]]
            (let [token (:token db)
                  headers {"Authorization" (str "Bearer " token)}
                  list-params {:offset "0" :limit "10" :collection_id collection-id}]
              {:db db
               :http-xhrio (api/secret-update id headers params [::edit-secret-ok list-params] [::edit-secret-not-ok])})))

(rf/reg-event-fx
 ::edit-secret-not-ok
 (fn-traced [{:keys [db]} [_ response]]
            {:db (-> db
                     (assoc-in [:secrets :visibility :loading?] false)
                     (assoc-in [:secrets :result :success] false)
                     (assoc-in [:secrets :result :response :body] (keywordize-keys (:response response)))
                     (assoc-in [:secrets :result :response :status] (:status response)))
             :dispatch [::events/display-response-errors response]}))

(rf/reg-event-fx
 ::edit-secret-ok
 (fn-traced [{:keys [db]} [_ params _]]
            {:db (-> db
                     (assoc-in [:secrets :result :success] true)
                     (assoc-in [:secrets :visibility :display-editor?] nil)
                     (assoc-in [:secrets :selected-id] nil)
                     (assoc-in [:secrets :selected-secret-type] nil)
                     (assoc-in [:secrets :detail] nil))
             :dispatch [::get-secrets params]}))

(rf/reg-event-fx
 ::delete-secret
 (fn-traced [{:keys [db]} [_ collection-id id]]
            (let [token (:token db)
                  headers {"Authorization" (str "Bearer " token)}
                  list-params {:offset "0" :limit "10" :collection_id collection-id}]
              {:db (-> db
                       (assoc-in [:secrets :selected-id] nil)
                       (assoc-in [:secrets :selected-secret-type] nil)
                       (assoc-in [:secrets :detail] nil))
               :http-xhrio (api/secret-delete id headers [::delete-secret-ok list-params] [::delete-secret-not-ok])})))

(rf/reg-event-fx
 ::delete-secret-not-ok
 (fn-traced [{:keys [db]} [_ response]]
            {:db (-> db
                     (assoc-in [:secrets :visibility :loading?] false)
                     (assoc-in [:secrets :result :success] false)
                     (assoc-in [:secrets :result :response :body] (keywordize-keys (:response response)))
                     (assoc-in [:secrets :result :response :status] (:status response)))
             :dispatch [::events/display-response-errors response]}))

(rf/reg-event-fx
 ::delete-secret-ok
 (fn-traced [{:keys [db]} [_ params _]]
            {:db (assoc-in db [:secrets :result :success] true)
             :dispatch [::get-secrets params]}))

(rf/reg-event-db
 ::display-editor
 (fn-traced [db [_ val]]
            (assoc-in db [:secrets :visibility :display-editor?] val)))

(rf/reg-event-db
 ::editing?
 (fn-traced [db [_ val]]
            (assoc-in db [:secrets :visibility :editing?] val)))

(rf/reg-event-db
 ::select-secret
 (fn-traced [db [_ id]]
            (assoc-in db [:secrets :selected-id] id)))

(rf/reg-event-db
 ::set-selected-secret-type
 (fn-traced [db [_ type]]
            (assoc-in db [:secrets :selected-secret-type] type)))

(rf/reg-event-db
 ::search-hashes
 (fn-traced [db [_ hashes]]
            (assoc-in db [:secrets :search-hashes] hashes)))
