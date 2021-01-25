(ns cljs.script
  (:require ["process" :as process]
            ["path" :as path]
            ["@pact-foundation/pact" :as pact]
            ["jsonwebtoken" :as jwt]))

(def pact-file (.resolve path "./.pacts/bilggeui-bilggeapi.json"))

(def cwd (.cwd process))

(def pact-opts
  #js {:log (.resolve path cwd ".logs" "pact.log")
       :dir (.resolve path cwd ".pacts")
       :logLevel "INFO"
       :host "0.0.0.0"
       :port 9090
       :consumer "bilggeUi"
       :provider "bilggeApi"})

(def matchers pact/Matchers)
(def like #(.like matchers %))
(def each-like #(.eachLike matchers (clj->js %)))
(defn term [match generate] (.term matchers (clj->js {:generate generate
                                                      :matcher match})))

;; ---------------------------------------------------------------------------------------
(def valid-user "pact-verifier-user")
(def valid-user-id "75ab36d3-3552-407b-b64b-0c56424fc479")
(def valid-plain "0123456789abcdef0123456789abcdef")
(def jwt-ci-secret "jwt-ci-secret")
(def valid-token (.sign jwt (clj->js {:username valid-user :user_id valid-user-id}) jwt-ci-secret (clj->js {:expiresIn "10y"})))
(def bearer (str "Bearer " valid-token))
(def valid-bearer (term "Bearer\\s[a-zA-Z0-9._-]{1,}$" bearer))
(def login-cipher "JoVq5oVBVxB2k/kdFsxoowLidnOwm4gJwSMijAMKKUKW/f1Gec5OA/YogjQSmoJOYP+GcPAb4IpKZIsFvrrrchbKdCY8lu4tE9iG2TKxeXTx0nnFBKsP5W9w5RXyqUXTHpTu51W8IG+LoItxmi3zvWbEeuQY+AcO8vAorP47KJ8=")
;; ---------------------------------------------------------------------------------------

(def register-success
  (clj->js {:uponReceiving "success post /register"
            :withRequest {:method "POST"
                          :path "/register"
                          :headers {"Content-Type" "application/json"}
                          :body {:username (like "created-by-pact")
                                 :public_key "test-public-key"
                                 :key "test-key"
                                 :salt "test-salt"}}
            :willRespondWith {:status 201}}))

(def register-validation-error
  (clj->js {:uponReceiving "invalid post /register"
            :withRequest {:method "POST"
                          :path "/register"
                          :headers {"Content-Type" "application/json"}
                          :body {:username "yb"
                                 :public_key ""
                                 :key ""
                                 :salt ""}}
            :willRespondWith {:status 400
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "validation"
                                     :messages ["invalid username"
                                                "public_key can not be empty"
                                                "key can not be empty"
                                                "salt can not be empty"]}}}))

;; ---------------------------------------------------------------------------------------

(def login-request-not-found
  (clj->js {:uponReceiving "not-existing user posts /login/request"
            :withRequest {:method "POST"
                          :path "/login/request"
                          :headers {"Content-Type" "application/json"}
                          :body {:username "not-existing"}}
            :willRespondWith {:status 404
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "not_found"
                                     :messages ["user not found"]}}}))

(def login-request-success
  (clj->js {:uponReceiving "success post /login/request"
            :withRequest {:method "POST"
                          :path "/login/request"
                          :headers {"Content-Type" "application/json"}
                          :body {:username "ybaroj"}}
            :willRespondWith {:status 200
                              :headers {"Content-Type" "application/json"}
                              :body {:cipher (like login-cipher)}}}))

(def login-authenticate-invalid
  (clj->js {:uponReceiving "invalid plain text post /login/authenticate"
            :withRequest {:method "POST"
                          :path "/login/authenticate"
                          :headers {"Content-Type" "application/json"}
                          :body {:username valid-user
                                 :plain "invalid-plain-text"}}
            :willRespondWith {:status 400
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "validation"
                                     :messages ["plain does not match"]}}}))

(def login-authenticate-validation-error
  (clj->js {:uponReceiving "empty plain text post /login/authenticate"
            :withRequest {:method "POST"
                          :path "/login/authenticate"
                          :headers {"Content-Type" "application/json"}
                          :body {:username valid-user
                                 :plain ""}}
            :willRespondWith {:status 400
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "validation"
                                     :messages ["plain can not be empty"]}}}))

(def login-authenticate-success
  (clj->js {:uponReceiving "invalid plain text post /login/authenticate"
            :withRequest {:method "POST"
                          :path "/login/authenticate"
                          :headers {"Content-Type" "application/json"}
                          :body {:username valid-user
                                 :plain (like valid-plain)}}
            :willRespondWith {:status 200
                              :headers {"Content-Type" "application/json"}
                              :body {:token (like "your-jwt")
                                     :public_key (like "your-rsa-public-key")
                                     :key (like "your-rsa-encrypted-aes-key")
                                     :salt (like "your-rsa-encrypted-hash-salt")}}}))

;; ---------------------------------------------------------------------------------------

(def collections-unauthorized
  (clj->js {:uponReceiving "unauthorized get /collections"
            :withRequest {:method "GET"
                          :path "/collections"
                          :headers {"Authorization" "Bearer"}}
            :willRespondWith {:status 403}}))

(def collections-list
  (clj->js {:uponReceiving "get /collections"
            :withRequest {:method "GET"
                          :path "/collections"
                          :headers {"Authorization" valid-bearer}}
            :willRespondWith {:status 200
                              :headers {"Content-Type" "application/json"}
                              :body {:data (each-like {:id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                                                       :name "encrypted-name-1"
                                                       :_iv "encrypted-iv-1"})}}}))

(def collection-create-validation-error
  (clj->js {:uponReceiving "bad-request post /collections"
            :withRequest {:method "POST"
                          :path "/collections"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" valid-bearer}
                          :body {:name "" :_iv ""}}
            :willRespondWith {:status 400
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "validation"
                                     :messages ["name can not be empty"
                                                "_iv can not be empty"]}}}))

(def collection-create-success
  (clj->js {:uponReceiving "post /collections"
            :withRequest {:method "POST"
                          :path "/collections"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" valid-bearer}
                          :body {:name "encrypted-name" :_iv "encrypted-iv"}}
            :willRespondWith {:status 201}}))

(def collection-edit-not-found-error
  (clj->js {:uponReceiving "not-found put /collections/e413c43d-401e-4731-a80c-c87b050922a7"
            :withRequest {:method "PUT"
                          :path "/collections/e413c43d-401e-4731-a80c-c87b050922a7"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" valid-bearer}
                          :body {:name "encrypted-name" :_iv "encrypted-iv"}}
            :willRespondWith {:status 404
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "not_found"
                                     :messages ["collection not found"]}}}))

(def collection-edit-validation-error
  (clj->js {:uponReceiving "bad-request put /collections/5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
            :withRequest {:method "PUT"
                          :path "/collections/5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" valid-bearer}
                          :body {:name "" :_iv ""}}
            :willRespondWith {:status 400
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "validation"
                                     :messages ["name can not be empty"
                                                "_iv can not be empty"]}}}))

(def collection-edit-success
  (clj->js {:uponReceiving "put /collections/5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
            :withRequest {:method "PUT"
                          :path "/collections/5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" valid-bearer}
                          :body {:name "enc-name" :_iv "enc-iv"}}
            :willRespondWith {:status 204}}))

(def collection-delete-not-found-error
  (clj->js {:uponReceiving "not-found delete /collections/e413c43d-401e-4731-a80c-c87b050922a7"
            :withRequest {:method "DELETE"
                          :path "/collections/e413c43d-401e-4731-a80c-c87b050922a7"
                          :headers {"Authorization" valid-bearer}}
            :willRespondWith {:status 404
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "not_found"
                                     :messages ["collection not found"]}}}))

(def collection-delete-success
  (clj->js {:uponReceiving "delete /collections/25bf8f6c-c228-4e6a-9a06-ec26f727a82f"
            :withRequest {:method "DELETE"
                          :path "/collections/25bf8f6c-c228-4e6a-9a06-ec26f727a82f"
                          :headers {"Authorization" valid-bearer}}
            :willRespondWith {:status 204}}))

;; ---------------------------------------------------------------------------------------

(def secrets-unauthorized
  (clj->js {:uponReceiving "unauthorized get /secrets"
            :withRequest {:method "GET"
                          :path "/secrets"
                          :headers {"Authorization" "Bearer"}
                          :query {:collection_id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                                  :offset "0"
                                  :limit "10"
                                  :q ""}}
            :willRespondWith {:status 403}}))

(def secrets-list
  (clj->js {:uponReceiving "get /secrets"
            :withRequest {:method "GET"
                          :path "/secrets"
                          :headers {"Authorization" valid-bearer}
                          :query {:collection_id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                                  :offset "0"
                                  :limit "10"
                                  :q ""}}
            :willRespondWith {:status 200
                              :headers {"Content-Type" "application/json"}
                              :body {:pagination {:total (like 1)
                                                  :offset 0
                                                  :limit 10}
                                     :data (each-like {:id "528bd2c2-9fc2-471b-866d-e19152a041e3"
                                                       :type "encrypted-type-1"
                                                       :title "encrypted-title-1"
                                                       :_iv "encrypted-iv-1"})}}}))

(def secret-create-validation-error
  (clj->js {:uponReceiving "bad-request post /secrets"
            :withRequest {:method "POST"
                          :path "/secrets"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" valid-bearer}
                          :body {:collection_id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8" :type "" :title "" :content "" :_iv "" :hashes []}}
            :willRespondWith {:status 400
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "validation"
                                     :messages ["type can not be empty"
                                                "title can not be empty"
                                                "content can not be empty"
                                                "_iv can not be empty"
                                                "hashes can not be empty"]}}}))

(def secret-create-success
  (clj->js {:uponReceiving "post /secrets"
            :withRequest {:method "POST"
                          :path "/secrets"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" valid-bearer}
                          :body {:collection_id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                                 :type "new-enc-type"
                                 :title "new-enc-title"
                                 :content "new-enc-content"
                                 :_iv "new-enc-iv"
                                 :hashes ["title-hash-1"]}}
            :willRespondWith {:status 201}}))

(def secret-detail-not-found
  (clj->js {:uponReceiving "not-found get /secrets/9a50af13-b8f7-44cf-ad07-5a2fefc1db22"
            :withRequest {:method "GET"
                          :path "/secrets/9a50af13-b8f7-44cf-ad07-5a2fefc1db22"
                          :headers {"Authorization" valid-bearer}}
            :willRespondWith {:status 404
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "not_found"
                                     :messages ["secret not found"]}}}))

(def secret-detail
  (clj->js {:uponReceiving "get /secrets/2b08c749-a996-44b6-9d12-9398b3789861"
            :withRequest {:method "GET"
                          :path "/secrets/2b08c749-a996-44b6-9d12-9398b3789861"
                          :headers {"Authorization" valid-bearer}}
            :willRespondWith {:status 200
                              :headers {"Content-Type" "application/json"}
                              :body {:id (like "2b08c749-a996-44b6-9d12-9398b3789861")
                                     :collection_id (like "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8")
                                     :type (like "new-enc-type")
                                     :title (like "new-enc-title")
                                     :content (like "new-enc-content")
                                     :_iv (like "new-enc-iv")}}}))

(def secret-edit-not-found-error
  (clj->js {:uponReceiving "not-found put /secrets/9a50af13-b8f7-44cf-ad07-5a2fefc1db22"
            :withRequest {:method "PUT"
                          :path "/secrets/9a50af13-b8f7-44cf-ad07-5a2fefc1db22"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" valid-bearer}
                          :body {:collection_id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                                 :type "new-enc-type"
                                 :title "new-enc-title"
                                 :content "new-enc-content"
                                 :_iv "new-enc-iv"
                                 :hashes ["title-hash-1"]}}
            :willRespondWith {:status 404
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "not_found"
                                     :messages ["secret not found"]}}}))

(def secret-edit-validation-error
  (clj->js {:uponReceiving "bad-request put /secrets/2b08c749-a996-44b6-9d12-9398b3789861"
            :withRequest {:method "PUT"
                          :path "/secrets/2b08c749-a996-44b6-9d12-9398b3789861"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" valid-bearer}
                          :body {:collection_id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                                 :type ""
                                 :title ""
                                 :content ""
                                 :_iv ""
                                 :hashes []}}
            :willRespondWith {:status 400
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "validation"
                                     :messages ["type can not be empty"
                                                "title can not be empty"
                                                "content can not be empty"
                                                "_iv can not be empty"
                                                "hashes can not be empty"]}}}))

(def secret-edit-success
  (clj->js {:uponReceiving "put /secrets/2b08c749-a996-44b6-9d12-9398b3789861"
            :withRequest {:method "PUT"
                          :path "/secrets/2b08c749-a996-44b6-9d12-9398b3789861"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" valid-bearer}
                          :body {:collection_id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                                 :type "new-enc-type"
                                 :title "new-enc-title"
                                 :content "new-enc-content"
                                 :_iv "new-enc-iv"
                                 :hashes ["title-hash-1"]}}
            :willRespondWith {:status 204}}))

(def secret-delete-not-found-error
  (clj->js {:uponReceiving "not-found delete /secrets/9a50af13-b8f7-44cf-ad07-5a2fefc1db22"
            :withRequest {:method "DELETE"
                          :path "/secrets/9a50af13-b8f7-44cf-ad07-5a2fefc1db22"
                          :headers {"Authorization" valid-bearer}}
            :willRespondWith {:status 404
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "not_found"
                                     :messages ["secret not found"]}}}))

(def secret-delete-success
  (clj->js {:uponReceiving "delete /secrets/2b08c749-a996-44b6-9d12-9398b3789861"
            :withRequest {:method "DELETE"
                          :path "/secrets/2b08c749-a996-44b6-9d12-9398b3789861"
                          :headers {"Authorization" valid-bearer}}
            :willRespondWith {:status 204}}))

;; ---------------------------------------------------------------------------------------
(defn pact-server
  []
  (let [provider (pact/Pact. pact-opts)]
    (-> provider
        .setup
        (.then #(do
                  (.addInteraction provider register-success)
                  (.addInteraction provider register-validation-error)
                  (.addInteraction provider login-request-not-found)
                  (.addInteraction provider login-request-success)
                  (.addInteraction provider login-authenticate-invalid)
                  (.addInteraction provider login-authenticate-validation-error)
                  (.addInteraction provider login-authenticate-success)
                  (.addInteraction provider collections-unauthorized)
                  (.addInteraction provider collections-list)
                  (.addInteraction provider collection-create-validation-error)
                  (.addInteraction provider collection-create-success)
                  (.addInteraction provider collection-edit-not-found-error)
                  (.addInteraction provider collection-edit-validation-error)
                  (.addInteraction provider collection-edit-success)
                  (.addInteraction provider collection-delete-not-found-error)
                  (.addInteraction provider collection-delete-success)
                  (.addInteraction provider secrets-unauthorized)
                  (.addInteraction provider secrets-list)
                  (.addInteraction provider secret-create-validation-error)
                  (.addInteraction provider secret-create-success)
                  (.addInteraction provider secret-detail-not-found)
                  (.addInteraction provider secret-detail)
                  (.addInteraction provider secret-edit-not-found-error)
                  (.addInteraction provider secret-edit-validation-error)
                  (.addInteraction provider secret-edit-success)
                  (.addInteraction provider secret-delete-not-found-error)
                  (.addInteraction provider secret-delete-success))))
    (.on process "SIGINT" #(do
                             (.finalize provider)
                             (.removeAllServers pact)))))

(def publish-opts
  (clj->js {:pactFilesOrDirs [pact-file]
            :consumerVersion (.. process -env -CONSUMER_VERSION)
            :pactBroker (.. process -env -PACT_BROKER)
            :pactBrokerToken (.. process -env -PACT_BROKER_TOKEN)}))

(defn pact-publish
  []
  (let [publisher (pact/Publisher. publish-opts)]
    (prn publish-opts)
    (.publishPacts publisher)))

(defn main
  [& args]
  (case (first args)
    "server" (pact-server)
    "publish" (pact-publish)))
