(ns cljs.script
  (:require ["process" :as process]
            ["path" :as path]
            ["@pact-foundation/pact" :as pact]))

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

;; ---------------------------------------------------------------------------------------

(def register-success
  (clj->js {:uponReceiving "success post /register"
            :withRequest {:method "POST"
                          :path "/register"
                          :headers {"Content-Type" "application/json"}
                          :body {:username "ybaroj"
                                 :public_key "test-public-key"
                                 :key "test-key"
                                 :salt "test-salt"}}
            :willRespondWith {:status 201
                              :body {:username "ybaroj"}}}))

(def register-validation-error
  (clj->js {:uponReceiving "invalid post /register"
            :withRequest {:method "POST"
                          :path "/register"
                          :headers {"Content-Type" "application/json"}
                          :body {:username "yb"
                                 :public_key nil
                                 :key ""
                                 :salt nil}}
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
                              :body {:cipher "test-cipher"}}}))

(def login-authenticate-invalid
  (clj->js {:uponReceiving "invalid plain text post /login/authenticate"
            :withRequest {:method "POST"
                          :path "/login/authenticate"
                          :headers {"Content-Type" "application/json"}
                          :body {:username "ybaroj"
                                 :plain "base64-encoded-invalid-plain-text"}}
            :willRespondWith {:status 401
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "decryption"
                                     :messages ["plain does not match"]}}}))

(def login-authenticate-validation-error
  (clj->js {:uponReceiving "empty plain text post /login/authenticate"
            :withRequest {:method "POST"
                          :path "/login/authenticate"
                          :headers {"Content-Type" "application/json"}
                          :body {:username "yb"
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
                          :body {:username "ybaroj"
                                 :plain "base64-encoded-valid-plain"}}
            :willRespondWith {:status 200
                              :headers {"Content-Type" "application/json"}
                              :body {:token "your-jwt"
                                     :public_key "your-rsa-public-key"
                                     :key "your-rsa-encrypted-aes-key"
                                     :salt "your-rsa-encrypted-hash-salt"}}}))

;; ---------------------------------------------------------------------------------------

(def collections-unauthorized
  (clj->js {:uponReceiving "unauthorized get /collections"
            :withRequest {:method "GET"
                          :path "/collections"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" "Bearer"}}
            :willRespondWith {:status 401
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "authorization"
                                     :messages ["permission denied"]}}}))

(def collections-list
  (clj->js {:uponReceiving "get /collections"
            :withRequest {:method "GET"
                          :path "/collections"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" "Bearer valid-jwt"}}
            :willRespondWith {:status 200
                              :headers {"Content-Type" "application/json"}
                              :body {:data [{:id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                                             :name "encrypted-name-1"
                                             :_iv "encrypted-iv-1"}
                                            {:id "cd94c1bf-6af8-47f7-a03d-161cbf9dd868"
                                             :name "encrypted-name-2"
                                             :_iv "encrypted-iv-2"}]}}}))

(def collection-create-validation-error
  (clj->js {:uponReceiving "bad-request post /collections"
            :withRequest {:method "POST"
                          :path "/collections"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" "Bearer valid-jwt"}
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
                                    "Authorization" "Bearer valid-jwt"}
                          :body {:name "encrypted-name" :_iv "encrypted-iv"}}
            :willRespondWith {:status 201
                              :headers {"Content-Type" "application/json"}
                              :body {:id "2b08c749-a996-44b6-9d12-9398b3789861"
                                     :name "new-enc-name"
                                     :_iv "new-enc-iv"}}}))

(def collection-edit-not-found-error
  (clj->js {:uponReceiving "not-found put /collections/e413c43d-401e-4731-a80c-c87b050922a7"
            :withRequest {:method "PUT"
                          :path "/collections/e413c43d-401e-4731-a80c-c87b050922a7"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" "Bearer valid-jwt"}
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
                                    "Authorization" "Bearer valid-jwt"}
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
                                    "Authorization" "Bearer valid-jwt"}
                          :body {:name "enc-name" :_iv "enc-iv"}}
            :willRespondWith {:status 200
                              :headers {"Content-Type" "application/json"}
                              :body {:id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                                     :name "enc-name"
                                     :_iv "enc-iv"}}}))

(def collection-delete-not-found-error
  (clj->js {:uponReceiving "not-found delete /collections/e413c43d-401e-4731-a80c-c87b050922a7"
            :withRequest {:method "DELETE"
                          :path "/collections/e413c43d-401e-4731-a80c-c87b050922a7"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" "Bearer valid-jwt"}
                          :body {:id "e413c43d-401e-4731-a80c-c87b050922a7"}}
            :willRespondWith {:status 404
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "not_found"
                                     :messages ["collection not found"]}}}))

(def collection-delete-success
  (clj->js {:uponReceiving "delete /collections/5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
            :withRequest {:method "DELETE"
                          :path "/collections/5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" "Bearer valid-jwt"}
                          :body {:id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"}}
            :willRespondWith {:status 200
                              :headers {"Content-Type" "application/json"}
                              :body {:id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"}}}))

;; ---------------------------------------------------------------------------------------

(def secrets-unauthorized
  (clj->js {:uponReceiving "unauthorized get /secrets"
            :withRequest {:method "GET"
                          :path "/secrets"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" "Bearer"}
                          :query {:collection_id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                                  :page "1"}}
            :willRespondWith {:status 401
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "authorization"
                                     :messages ["permission denied"]}}}))

(def secrets-list
  (clj->js {:uponReceiving "get /secrets"
            :withRequest {:method "GET"
                          :path "/secrets"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" "Bearer valid-jwt"}
                          :query {:collection_id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                                  :page "1"}}
            :willRespondWith {:status 200
                              :headers {"Content-Type" "application/json"}
                              :body {:pagination {:page 1
                                                  :count 1}
                                     :data [{:id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                                             :type "encrypted-type-1"
                                             :title "encrypted-title-1"
                                             :_iv "encrypted-iv-1"
                                             :modified_at 1610230065}]}}}))

(def secret-create-validation-error
  (clj->js {:uponReceiving "bad-request post /secrets"
            :withRequest {:method "POST"
                          :path "/secrets"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" "Bearer valid-jwt"}
                          :body {:collection_id "" :type "" :title "" :content "" :_iv "" :hashes []}}
            :willRespondWith {:status 400
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "validation"
                                     :messages ["collection_id can not be empty"
                                                "type can not be empty"
                                                "title can not be empty"
                                                "content can not be empty"
                                                "_iv can not be empty"
                                                "hashes can not be empty"]}}}))

(def secret-create-success
  (clj->js {:uponReceiving "post /secrets"
            :withRequest {:method "POST"
                          :path "/secrets"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" "Bearer valid-jwt"}
                          :body {:collection_id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                                 :type "new-enc-type"
                                 :title "new-enc-title"
                                 :content "new-enc-content"
                                 :_iv "new-enc-iv"
                                 :hashes ["title-hash-1"]}}
            :willRespondWith {:status 201
                              :headers {"Content-Type" "application/json"}
                              :body {:id "2b08c749-a996-44b6-9d12-9398b3789861"
                                     :type "new-enc-type"
                                     :title "new-enc-title"
                                     :_iv "new-enc-iv"}}}))

(def secret-detail-not-found
  (clj->js {:uponReceiving "not-found get /secrets/9a50af13-b8f7-44cf-ad07-5a2fefc1db22"
            :withRequest {:method "GET"
                          :path "/secrets/9a50af13-b8f7-44cf-ad07-5a2fefc1db22"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" "Bearer valid-jwt"}}
            :willRespondWith {:status 404
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "not_found"
                                     :messages ["secret not found"]}}}))

(def secret-detail
  (clj->js {:uponReceiving "get /secrets/2b08c749-a996-44b6-9d12-9398b3789861"
            :withRequest {:method "GET"
                          :path "/secrets/2b08c749-a996-44b6-9d12-9398b3789861"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" "Bearer valid-jwt"}}
            :willRespondWith {:status 200
                              :headers {"Content-Type" "application/json"}
                              :body {:id "2b08c749-a996-44b6-9d12-9398b3789861"
                                     :collection_id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                                     :type "new-enc-type"
                                     :title "new-enc-title"
                                     :content "new-enc-content"
                                     :_iv "new-enc-iv"}}}))

(def secret-edit-not-found-error
  (clj->js {:uponReceiving "not-found put /secrets/9a50af13-b8f7-44cf-ad07-5a2fefc1db22"
            :withRequest {:method "PUT"
                          :path "/secrets/9a50af13-b8f7-44cf-ad07-5a2fefc1db22"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" "Bearer valid-jwt"}
                          :body {:type "new-enc-type"
                                 :title "new-enc-title"
                                 :content "new-enc-content"
                                 :_iv "new-enc-iv"}}
            :willRespondWith {:status 404
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "not_found"
                                     :messages ["secret not found"]}}}))

(def secret-edit-validation-error
  (clj->js {:uponReceiving "bad-request put /secrets/2b08c749-a996-44b6-9d12-9398b3789861"
            :withRequest {:method "PUT"
                          :path "/secrets/2b08c749-a996-44b6-9d12-9398b3789861"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" "Bearer valid-jwt"}
                          :body {:type ""
                                 :title ""
                                 :content ""
                                 :_iv ""
                                 :hashes []}}
            :willRespondWith {:status 404
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
                                    "Authorization" "Bearer valid-jwt"}
                          :body {:type "new-enc-type"
                                 :title "new-enc-title"
                                 :content "new-enc-content"
                                 :_iv "new-enc-iv"
                                 :hashes ["title-hash-1"]}}
            :willRespondWith {:status 200
                              :headers {"Content-Type" "application/json"}
                              :body {:id "2b08c749-a996-44b6-9d12-9398b3789861"
                                     :type "new-enc-type"
                                     :title "new-enc-title"
                                     :_iv "new-enc-iv"}}}))

(def secret-delete-not-found-error
  (clj->js {:uponReceiving "not-found delete /secrets/9a50af13-b8f7-44cf-ad07-5a2fefc1db22"
            :withRequest {:method "DELETE"
                          :path "/secrets/9a50af13-b8f7-44cf-ad07-5a2fefc1db22"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" "Bearer valid-jwt"}
                          :body {:id "9a50af13-b8f7-44cf-ad07-5a2fefc1db22"}}
            :willRespondWith {:status 404
                              :headers {"Content-Type" "application/json"}
                              :body {:reason "not_found"
                                     :messages ["secret not found"]}}}))

(def secret-delete-success
  (clj->js {:uponReceiving "delete /secrets/2b08c749-a996-44b6-9d12-9398b3789861"
            :withRequest {:method "DELETE"
                          :path "/secrets/2b08c749-a996-44b6-9d12-9398b3789861"
                          :headers {"Content-Type" "application/json"
                                    "Authorization" "Bearer valid-jwt"}
                          :body {:id "2b08c749-a996-44b6-9d12-9398b3789861"}}
            :willRespondWith {:status 200
                              :headers {"Content-Type" "application/json"}
                              :body {:id "2b08c749-a996-44b6-9d12-9398b3789861"}}}))

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
