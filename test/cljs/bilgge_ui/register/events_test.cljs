(ns bilgge-ui.register.events-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [re-frame.core :as rf]
            [day8.re-frame.test :as rf-test]
            [bilgge-ui.events :as e]
            [bilgge-ui.register.events :as r-e]
            [bilgge-ui.register.subs :as r-s]))

(deftest success-register-events
  (rf-test/run-test-async
    (rf/dispatch-sync [::e/initialize-db])

    (let [success? (rf/subscribe [::r-s/success?])
          params {:username "ybaroj"
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
          expected-errors ["invalid username"
                           "public_key can not be empty"
                           "key can not be empty"
                           "salt can not be empty"]
          params {:username "yb"
                  :public_key nil
                  :key ""
                  :salt nil}]

      (rf/dispatch [::r-e/register params])
      (rf-test/wait-for [::r-e/register-not-ok]
                        (is (false? @success?))
                        (is (= "validation" (:reason @response)))
                        (is (true? (every? #(-> #{%} (some (:messages @response)) some?) expected-errors)))))))
