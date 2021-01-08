(ns bilgge-ui.api
  (:require [ajax.core :as ajax]))

(goog-define API-BASE-URL "")

(defn- get-uri
  [path]
  (str API-BASE-URL path))

(defn- ->http-xhrio
  ([method uri headers params on-success-v on-failure-v]
   {:method method
    :uri uri
    :headers headers
    :params params
    :format (ajax/json-request-format)
    :response-format (ajax/json-response-format {:keywords? true})
    :on-success on-success-v
    :on-failure on-failure-v})
  ([method uri params on-success-v on-failure-v]
   (->http-xhrio method uri nil params on-success-v on-failure-v)))

;; ---------------------------------------------------------------------------------------

(defn register
  [params on-success-v on-failure-v]
  (let [uri (get-uri "/register")]
    (->http-xhrio :post uri params on-success-v on-failure-v)))

(defn login-request
  [params on-success-v on-failure-v]
  (let [uri (get-uri "/login/request")]
    (->http-xhrio :post uri params on-success-v on-failure-v)))

(defn login-authenticate
  [params on-success-v on-failure-v]
  (let [uri (get-uri "/login/authenticate")]
    (->http-xhrio :post uri params on-success-v on-failure-v)))

;; ---------------------------------------------------------------------------------------

(defn- collections*
  [method headers params on-success-v on-failure-v]
  (let [uri (get-uri "/collections")]
    (->http-xhrio method uri headers params on-success-v on-failure-v)))

(defn collections-list
  [headers params on-success-v on-failure-v]
  (collections* :get headers params on-success-v on-failure-v))

(defn collections-create
  [headers params on-success-v on-failure-v]
  (collections* :post headers params on-success-v on-failure-v))

;; ---------------------------------------------------------------------------------------

(defn- collection*
  [id method headers params on-success-v on-failure-v]
  (let [uri (str (get-uri "/collections/") id)]
    (->http-xhrio method uri headers params on-success-v on-failure-v)))

(defn collection-update
  [id headers params on-success-v on-failure-v]
  (collection* id :put headers params on-success-v on-failure-v))

(defn collection-delete
  [id headers on-success-v on-failure-v]
  (collection* id :delete headers nil on-success-v on-failure-v))

;; ---------------------------------------------------------------------------------------

(defn- secrets*
  [method headers params on-success-v on-failure-v]
  (let [uri (get-uri "/secrets")]
    (->http-xhrio method uri headers params on-success-v on-failure-v)))

(defn secrets-list
  [headers params on-success-v on-failure-v]
  (secrets* :get headers params on-success-v on-failure-v))

(defn secrets-create
  [headers params on-success-v on-failure-v]
  (secrets* :post headers params on-success-v on-failure-v))

;; ---------------------------------------------------------------------------------------

(defn- secret*
  [id method headers params on-success-v on-failure-v]
  (let [uri (str (get-uri "/secrets/") id)]
    (->http-xhrio method uri headers params on-success-v on-failure-v)))

(defn secret-detail
  [id headers on-success-v on-failure-v]
  (secret* id :get headers nil on-success-v on-failure-v))

(defn secret-update
  [id headers params on-success-v on-failure-v]
  (secret* id :put headers params on-success-v on-failure-v))

(defn secret-delete
  [id headers on-success-v on-failure-v]
  (secret* id :delete headers nil on-success-v on-failure-v))
