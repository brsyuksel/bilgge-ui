(ns bilgge-ui.collections.events-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [re-frame.core :as rf]
            [day8.re-frame.test :as rf-test]
            [bilgge-ui.events :as e]
            [bilgge-ui.subs :as s]
            [bilgge-ui.collections.events :as c-e]
            [bilgge-ui.collections.subs :as c-s]))

(rf/reg-event-db
  ::initialize-test-db
  (fn [db [_ data]]
    data))

(deftest collection-list-unauthorized
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token ""}])

    (let [success? (rf/subscribe [::c-s/success?])
          response (rf/subscribe [::c-s/response-body])]

      (rf/dispatch [::c-e/get-collections])
      (rf-test/wait-for [::c-e/get-collections-not-ok]
                        (is (false? @success?))
                        (is (= "authorization" (:reason @response)))
                        (is (= ["permission denied"] (:messages @response)))))))

(deftest collection-list
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token "valid-jwt"
                                             :private-key "pk"
                                             :key "k"}])

    (let [success? (rf/subscribe [::c-s/success?])
          data (rf/subscribe [::c-s/data])
          expected-names ["encrypted-name-1"]
          expected-ivs ["encrypted-iv-1"]
          expected-ids ["5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"]]

      (rf/dispatch [::c-e/get-collections])
      (rf-test/wait-for [::c-e/get-collections-ok]
                        (is (true? @success?))
                        (is (true? (every? #(-> #{%} (some (map :name @data)) some?) expected-names)))
                        (is (true? (every? #(-> #{%} (some (map :id @data)) some?) expected-ids)))
                        (is (true? (every? #(-> #{%} (some (map :_iv @data)) some?) expected-ivs)))))))

(deftest collection-create-validation-error
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token "valid-jwt"
                                             :private-key "pk"
                                             :key "k"}])

    (let [success? (rf/subscribe [::c-s/success?])
          response (rf/subscribe [::c-s/response-body])
          params {:name "" :_iv ""}
          expected-messages ["name can not be empty"
                             "_iv can not be empty"]]

      (rf/dispatch [::c-e/create-collection params])
      (rf-test/wait-for [::c-e/create-collection-not-ok]
                        (is (false? @success?))
                        (is (= "validation" (:reason @response)))
                        (is (true? (every? #(-> #{%} (some (:messages @response)) some?) expected-messages)))))))

(deftest collection-create
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token "valid-jwt"
                                             :private-key "pk"
                                             :key "k"}])

    (let [success? (rf/subscribe [::c-s/success?])
          params {:name "encrypted-name" :_iv "encrypted-iv"}]

      (rf/dispatch [::c-e/create-collection params])
      (rf-test/wait-for [::c-e/create-collection-ok]
                        (is (true? @success?))))))

(deftest collection-edit-not-found-error
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token "valid-jwt"
                                             :private-key "pk"
                                             :key "k"}])

    (let [success? (rf/subscribe [::c-s/success?])
          response (rf/subscribe [::c-s/response-body])
          id "e413c43d-401e-4731-a80c-c87b050922a7"
          params {:name "encrypted-name" :_iv "encrypted-iv"}]

      (rf/dispatch [::c-e/edit-collection id params])
      (rf-test/wait-for [::c-e/edit-collection-not-ok]
                        (is (false? @success?))
                        (is (= "not_found" (:reason @response)))
                        (is (= ["collection not found"] (:messages @response)))))))


(deftest collection-edit-validation-error
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token "valid-jwt"
                                             :private-key "pk"
                                             :key "k"}])

    (let [success? (rf/subscribe [::c-s/success?])
          response (rf/subscribe [::c-s/response-body])
          id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
          params {:name "" :_iv ""}
          expected-messages ["name can not be empty"
                             "_iv can not be empty"]]

      (rf/dispatch [::c-e/edit-collection id params])
      (rf-test/wait-for [::c-e/edit-collection-not-ok]
                        (is (false? @success?))
                        (is (= "validation" (:reason @response)))
                        (is (true? (every? #(-> #{%} (some (:messages @response)) some?) expected-messages)))))))

(deftest collection-edit
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token "valid-jwt"
                                             :private-key "pk"
                                             :key "k"}])

    (let [success? (rf/subscribe [::c-s/success?])
          id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"
          params {:name "enc-name" :_iv "enc-iv"}]

      (rf/dispatch [::c-e/edit-collection id params])
      (rf-test/wait-for [::c-e/edit-collection-ok]
                        (is (true? @success?))))))

(deftest collection-delete-not-found-error
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token "valid-jwt"
                                             :private-key "pk"
                                             :key "k"}])

    (let [success? (rf/subscribe [::c-s/success?])
          response (rf/subscribe [::c-s/response-body])
          id "e413c43d-401e-4731-a80c-c87b050922a7"]

      (rf/dispatch [::c-e/delete-collection id])
      (rf-test/wait-for [::c-e/delete-collection-not-ok]
                        (is (false? @success?))
                        (is (= "not_found" (:reason @response)))
                        (is (= ["collection not found"] (:messages @response)))))))

(deftest collection-delete
  (rf-test/run-test-async
    (rf/dispatch-sync [::initialize-test-db {:token "valid-jwt"
                                             :private-key "pk"
                                             :key "k"}])

    (let [success? (rf/subscribe [::c-s/success?])
          id "5f6a97a3-52eb-44b2-983f-de9fc5bea7b8"]

      (rf/dispatch [::c-e/delete-collection id])
      (rf-test/wait-for [::c-e/delete-collection-ok]
                        (is (true? @success?))))))
