(ns bilgge.register.events-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [re-frame.core :as rf]
            [day8.re-frame.test :as rf-test]
            [bilgge.events :as e]
            [bilgge.register.events :as r-e]
            [bilgge.register.subs :as r-s]))

(deftest success-register-events
  (rf-test/run-test-async
    (rf/dispatch-sync [::e/initialize-db])

    (let [success? (rf/subscribe [::r-s/success?])
          params {:username "created-by-pact"
                  :public_key "test-public-key"
                  :key "test-key"
                  :salt "test-salt"}]

      (rf/dispatch [::r-e/register params])
      (rf-test/wait-for [::r-e/register-ok]
                        (is (true? @success?))))))

(deftest fail-register-events
  (rf-test/run-test-async
    (rf/dispatch-sync [::e/initialize-db])

    (let [success? (rf/subscribe [::r-s/success?])
          response (rf/subscribe [::r-s/response-body])
          status (rf/subscribe [::r-s/response-status])
          expected-errors ["invalid username"
                           "public_key can not be empty"
                           "key can not be empty"
                           "salt can not be empty"]
          params {:username "yb"
                  :public_key ""
                  :key ""
                  :salt ""}]

      (rf/dispatch [::r-e/register params])
      (rf-test/wait-for [::r-e/register-not-ok]
                        (is (false? @success?))
                        (is (= 400 @status))
                        (is (= "validation" (:reason @response)))
                        (is (true? (every? #(-> #{%} (some (:messages @response)) some?) expected-errors)))))))
