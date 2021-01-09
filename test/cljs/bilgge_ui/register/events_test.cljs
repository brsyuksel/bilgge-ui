(ns bilgge-ui.register.events-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [re-frame.core :as rf]
            [day8.re-frame.test :as rf-test]
            [bilgge-ui.events :as e]
            [bilgge-ui.register.events :as r-e]
            [bilgge-ui.register.subs :as r-s]))

(def test-public-key
  "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCqtR6Mb/T3BlMBMn7iqNAE01wXBYVtnA4qiPkaUEkRrAlNLHmhQ5A1lYzKfykd2cYv1VKbzKQ1RzFzkEOKfokVM3ZWpIP6GSi+h9UX5p6FHzVOr3qN7wDmcMOIRYPHr81k/IS61ezIahwpisoQjWuJQWJr27y68HIQH+9Fr7QqEQIDAQAB")
(def test-key
  "Zc/RUJ0hGphl+T/EQ34x6oLsxur8WTjP4a2nM7fztnatxJXwM6ZVwkHK//WfChlLkWEAKLQ8BVMcHI82O96oLY+ujM+b79Go4xMUM1TC5W1zT5RXCX3Fh7hZYIcsbFC26ftLgBkDMOZemfNv1vFgOCjSVVNZf1IGWblREfL3N8Y=")
(def test-salt
  "CTTePGjU/5LJr2RzQ1uAqUUyvK7UrTjF8lmE59vWsNR3vNrxSDS+1qBqblDVZEiuWOAaPi5TkymLkWR5HpDvRRY5ceMF0BlHByWqkBbAaD3lKZv1W0HTROppygdgRi9LbL0GkZZ7uiMptq9iXl5hOrjb/2OUGnm4NgF4gcWx1JE=")

(deftest success-register-events
  (rf-test/run-test-async
    (rf/dispatch-sync [::e/initialize-db])

    (let [success? (rf/subscribe [::r-s/success?])
          params {:username "ybaroj"
                  :public_key test-public-key
                  :key test-key
                  :salt test-salt}]

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
