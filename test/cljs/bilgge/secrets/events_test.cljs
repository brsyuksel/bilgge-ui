(ns bilgge.secrets.events-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [re-frame.core :as rf]
            [day8.re-frame.test :as rf-test]
            [bilgge.events :as e]
            [bilgge.subs :as s]
            [bilgge.secrets.events :as s-e]
            [bilgge.secrets.subs :as s-s]))

(rf/reg-event-db
  ::initialize-test-db
  (fn [db [_ data]]
    data))

(deftest secret-list-unauthorized
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token ""}])

    (let [success? (rf/subscribe [::s-s/success?])
          status (rf/subscribe [::s-s/response-status])
          params {:collection_id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                  :offset "0"
                  :limit "10"}]

      (rf/dispatch [::s-e/get-secrets params])
      (rf-test/wait-for [::s-e/get-secrets-not-ok]
                        (is (false? @success?))
                        (is (= 403 @status))))))

(deftest secret-list
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token "valid-jwt"
                                             :private-key "pk"
                                             :key "k"}])

    (let [success? (rf/subscribe [::s-s/success?])
          data (rf/subscribe [::s-s/data])
          params {:collection_id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                  :offset "0"
                  :limit "10"}]

      (rf/dispatch [::s-e/get-secrets params])
      (rf-test/wait-for [::s-e/get-secrets-ok]
                        (is (true? @success?))
                        (is (= "encrypted-type-1" (-> @data first :type)))
                        (is (true? (every? #(some? %) (map :plain-title @data))))
                        (is (true? (every? #(some? %) (map :plain-type @data))))))))

(deftest secret-create-validation-error
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token "valid-jwt"
                                             :private-key "pk"
                                             :key "k"}])

    (let [success? (rf/subscribe [::s-s/success?])
          response (rf/subscribe [::s-s/response-body])
          params {:collection_id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                  :type ""
                  :title ""
                  :content ""
                  :_iv ""
                  :hashes []}
          expected-messages ["type can not be empty"
                             "title can not be empty"
                             "content can not be empty"
                             "_iv can not be empty"
                             "hashes can not be empty"]]

      (rf/dispatch [::s-e/create-secret params])
      (rf-test/wait-for [::s-e/create-secret-not-ok]
                        (is (false? @success?))
                        (is (= "validation" (:reason @response)))
                        (is (true? (every? #(-> #{%} (some (:messages @response)) some?) expected-messages)))))))

(deftest secret-create
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token "valid-jwt"
                                             :private-key "pk"
                                             :key "k"}])

    (let [success? (rf/subscribe [::s-s/success?])
          params {:collection_id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                  :type "new-enc-type"
                  :title "new-enc-title"
                  :content "new-enc-content"
                  :_iv "new-enc-iv"
                  :hashes ["title-hash-1"]}]

      (rf/dispatch [::s-e/create-secret params])
      (rf-test/wait-for [::s-e/create-secret-ok]
                        (is (true? @success?))))))

(deftest secret-detail-not-found-error
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token "valid-jwt"
                                             :private-key "pk"
                                             :key "k"}])

    (let [success? (rf/subscribe [::s-s/success?])
          response (rf/subscribe [::s-s/response-body])
          id "9a50af13-b8f7-44cf-ad07-5a2fefc1db22"]

      (rf/dispatch [::s-e/get-secret-detail id])
      (rf-test/wait-for [::s-e/get-secret-detail-not-ok]
                        (is (false? @success?))
                        (is (= "not_found" (:reason @response)))
                        (is (= ["secret not found"] (:messages @response)))))))

(deftest secret-detail
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token "valid-jwt"
                                             :private-key "pk"
                                             :key "k"}])

    (let [success? (rf/subscribe [::s-s/success?])
          plain (rf/subscribe [::s-s/plain])
          id "2b08c749-a996-44b6-9d12-9398b3789861"]

      (rf/dispatch [::s-e/get-secret-detail id])
      (rf-test/wait-for [::s-e/get-secret-detail-ok]
                        (is (true? @success?))
                        (is (= "new-enc-content" (:content @plain)))
                        (is (some? (:plain-content @plain)))))))

(deftest secret-edit-not-found-error
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token "valid-jwt"
                                             :private-key "pk"
                                             :key "k"}])

    (let [success? (rf/subscribe [::s-s/success?])
          response (rf/subscribe [::s-s/response-body])
          collection-id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
          id "9a50af13-b8f7-44cf-ad07-5a2fefc1db22"
          params {:collection_id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                  :type "new-enc-type"
                  :title "new-enc-title"
                  :content "new-enc-content"
                  :_iv "new-enc-iv"
                  :hashes ["title-hash-1"]}]

      (rf/dispatch [::s-e/edit-secret collection-id id params])
      (rf-test/wait-for [::s-e/edit-secret-not-ok]
                        (is (false? @success?))
                        (is (= "not_found" (:reason @response)))
                        (is (= ["secret not found"] (:messages @response)))))))

(deftest secret-edit-validation-error
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token "valid-jwt"
                                             :private-key "pk"
                                             :key "k"}])

    (let [success? (rf/subscribe [::s-s/success?])
          response (rf/subscribe [::s-s/response-body])
          collection-id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
          id "2b08c749-a996-44b6-9d12-9398b3789861"
          params {:collection_id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                  :type ""
                  :title ""
                  :content ""
                  :_iv ""
                  :hashes []}
          expected-messages ["type can not be empty"
                             "title can not be empty"
                             "content can not be empty"
                             "_iv can not be empty"
                             "hashes can not be empty"]]

      (rf/dispatch [::s-e/edit-secret collection-id id params])
      (rf-test/wait-for [::s-e/edit-secret-not-ok]
                        (is (false? @success?))
                        (is (= "validation" (:reason @response)))
                        (is (true? (every? #(-> #{%} (some (:messages @response)) some?) expected-messages)))))))

(deftest secret-edit
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token "valid-jwt"
                                             :private-key "pk"
                                             :key "k"}])

    (let [success? (rf/subscribe [::s-s/success?])
          collection-id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
          id "2b08c749-a996-44b6-9d12-9398b3789861"
          params {:collection_id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
                  :type "new-enc-type"
                  :title "new-enc-title"
                  :content "new-enc-content"
                  :_iv "new-enc-iv"
                  :hashes ["title-hash-1"]}]

      (rf/dispatch [::s-e/edit-secret collection-id id params])
      (rf-test/wait-for [::s-e/edit-secret-ok]
                        (is (true? @success?))))))

(deftest secret-delete-not-found-error
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token "valid-jwt"
                                             :private-key "pk"
                                             :key "k"}])

    (let [success? (rf/subscribe [::s-s/success?])
          response (rf/subscribe [::s-s/response-body])
          status (rf/subscribe [::s-s/response-status])
          collection-id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
          id "9a50af13-b8f7-44cf-ad07-5a2fefc1db22"]

      (rf/dispatch [::s-e/delete-secret collection-id id])
      (rf-test/wait-for [::s-e/delete-secret-not-ok]
                        (is (false? @success?))
                        (is (= 404 @status))
                        (is (= "not_found" (:reason @response)))
                        (is (= ["secret not found"] (:messages @response)))))))

(deftest secret-delete
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token "valid-jwt"
                                             :private-key "pk"
                                             :key "k"}])

    (let [success? (rf/subscribe [::s-s/success?])
          collection-id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
          id "2b08c749-a996-44b6-9d12-9398b3789861"]

      (rf/dispatch [::s-e/delete-secret collection-id id])
      (rf-test/wait-for [::s-e/delete-secret-ok]
                        (is (true? @success?))))))
