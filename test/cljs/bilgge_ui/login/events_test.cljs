(ns bilgge-ui.login.events-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [re-frame.core :as rf]
            [day8.re-frame.test :as rf-test]
            [bilgge-ui.events :as e]
            [bilgge-ui.subs :as s]
            [bilgge-ui.login.events :as l-e]
            [bilgge-ui.login.subs :as l-s]))

(deftest login-with-not-existing-user
  (rf-test/run-test-async
    (rf/dispatch-sync [::e/initialize-db])

    (let [success? (rf/subscribe [::l-s/success?])
          response (rf/subscribe [::l-s/response-body])]

      (rf/dispatch-sync [::l-e/set-form-data :username "not-existing"])

      (rf/dispatch [::l-e/login-request])
      (rf-test/wait-for [::l-e/login-request-not-ok]
                        (is (false? @success?))
                        (is (= "not_found" (:reason @response)))
                        (is (= ["user not found"] (:messages @response)))))))

(deftest login-decrypt-cipher
  (rf-test/run-test-async
    (rf/dispatch-sync [::e/initialize-db])

    (let [plain (rf/subscribe [::l-s/form-data :plain])]

      (rf/dispatch-sync [::l-e/set-form-data :username "ybaroj"])

      (rf/dispatch [::l-e/login-request])
      (rf-test/wait-for [::l-e/login-request-ok]
                        (is (some? @plain))))))

(deftest login-authenticate-with-invalid-plain
  (rf-test/run-test-async
    (rf/dispatch-sync [::e/initialize-db])

    (let [response (rf/subscribe [::l-s/response-body])]

      (rf/dispatch-sync [::l-e/set-form-data :username "ybaroj"])
      (rf/dispatch-sync [::l-e/set-form-data :plain "base64-encoded-invalid-plain-text"])

      (rf/dispatch [::l-e/login-authenticate])
      (rf-test/wait-for [::l-e/login-authenticate-not-ok]
                        (is (= "decryption" (:reason @response)))
                        (is (= ["plain does not match"] (:messages @response)))))))

#_(deftest login-authenticate-validation-error
  (rf-test/run-test-async
    (rf/dispatch-sync [::e/initialize-db])

    (let [response (rf/subscribe [::l-s/response-body])]

      (rf/dispatch-sync [::l-e/set-form-data :username "yb"])
      (rf/dispatch-sync [::l-e/set-form-data :plain ""])

      (rf/dispatch [::l-e/login-authenticate])
      (rf-test/wait-for [::l-e/login-authenticate-not-ok]
                        (is (= "validation" (:reason @response)))
                        (is (= ["plain can not be empty"] (:messages @response)))))))

(deftest login-authenticate-success
  (rf-test/run-test-async
    (rf/dispatch-sync [::e/initialize-db])

    (let [success? (rf/subscribe [::l-s/success?])
          token (rf/subscribe [::s/token])]

      (rf/dispatch-sync [::l-e/set-form-data :username "ybaroj"])
      (rf/dispatch-sync [::l-e/set-form-data :plain "base64-encoded-valid-plain"])

      (rf/dispatch [::l-e/login-authenticate])
      (rf-test/wait-for [::l-e/login-authenticate-ok]
                        (is (true? @success?))
                        (is (= "your-jwt" @token))))))
