(ns bilgge.login.events-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [re-frame.core :as rf]
            [day8.re-frame.test :as rf-test]
            [bilgge.events :as e]
            [bilgge.subs :as s]
            [bilgge.login.events :as l-e]
            [bilgge.login.subs :as l-s]))

(def valid-private-key "MIICXAIBAAKBgHbEH/pdZfQqUDmkt5xX+gSH8aKPdDaDJs7pBmh9R9D6F+46oaMouMekrb4VLBQRP3OhrP+D+RmJUmQD0OOiUl32RosEAC3DGfjIRgYCNK5oWL/dcg+jCDXg4no19KrvVUkNkE3dqp2JMhIJF/d2k47xUdh5OoIlLl2MeveLpg+VAgMBAAECgYBu2Mf71ZYlmCR+WHUiT55DAlqNXZSamDftX+IiPeN8cR9KsqBP9t7XPqUGRx53sE1nb9tWM+eXZOpn/IPHIaX7TkyvTZy3DGo2ACl7HzjOEK/gqYLdUAETSPmjQvaLIcEF3jG0W0cgIVOrNP1aJoexRLHb5nnYq6OaN79B+OA7gQJBANYzZAb3QWGi1FaeUAPdjf7oZxbdyjsuTw37kZdPMqFOiaflyoXNv4+YJjRA2LSdg7LQ0q9UrJmWuWRVm+Y2aaECQQCN8TP+tovWtdTo66licq1PljN2Yyhk22pe4fPLyQX6mL95ymTjGr2FRL2RpdNV5OKbmbek6vkZ62FuAR1ivSl1AkEAgB4m4x+65Io/FTwFwfoft2sMVhn8nt84+7UPxP/i2aafIWSJePSyclHf7/slYwqfvjG3ApXT0t3bL48g+1ZqYQJAGFkp3CWgM0KZtSLHuZWGWUKgrUwxH6vrwT7tPSXMmsIdBl1LlRF/NR8njZZufCt5G8vwjp+n/2Q7IE2cptVgCQJBAJbLXDiWzZQD8SL79/ty7WBxoS5QSogUxH4UkcoDVRGNCxQLN/za9tyIZx0Lbu38SfYVlW902bhJuBy7V8jy4wk=")

(rf/reg-event-db
 ::initialize-test-db-with-data
 (fn [db [_ data]]
   data))

(deftest login-with-not-existing-user
  (rf-test/run-test-async
   (rf/dispatch-sync [::e/initialize-db])

   (let [success? (rf/subscribe [::l-s/success?])
         response (rf/subscribe [::l-s/response-body])
         status (rf/subscribe [::l-s/response-status])
         params {:username "not-existing"}]

     (rf/dispatch [::l-e/login-request params])
     (rf-test/wait-for [::l-e/login-request-not-ok]
                       (is (false? @success?))
                       (is (= 404 @status))
                       (is (= "not_found" (:reason @response)))
                       (is (= ["user not found"] (:messages @response)))))))

(deftest login-decrypt-cipher
  (rf-test/run-test-async
   (rf/dispatch-sync [::initialize-test-db-with-data {:private-key valid-private-key}])

   (let [plain (rf/subscribe [::l-s/data :plain])
         params {:username "ybaroj"}]

     (rf/dispatch [::l-e/login-request params])
     (rf-test/wait-for [::l-e/login-request-ok]
                       (is (some? @plain))))))

#_(deftest login-authenticate-with-invalid-plain
    (rf-test/run-test-async
     (rf/dispatch-sync [::e/initialize-db])

     (let [response (rf/subscribe [::l-s/response-body])
           params {:username "ybaroj"
                   :plain "base64-encoded-invalid-plain-text"}]

       (rf/dispatch-sync [::l-e/login-authenticate params])
       (rf-test/wait-for [::l-e/login-authenticate-not-ok]
                         (is (= "decryption" (:reason @response)))
                         (is (= ["plain does not match"] (:messages @response)))))))

#_(deftest login-authenticate-validation-error
    (rf-test/run-test-async
     (rf/dispatch-sync [::e/initialize-db])

     (let [response (rf/subscribe [::l-s/response-body])
           params {:username "yb" :plain ""}]

       (rf/dispatch-sync [::l-e/login-authenticate params])
       (rf-test/wait-for [::l-e/login-authenticate-not-ok]
                         (is (= "validation" (:reason @response)))
                         (is (= ["plain can not be empty"] (:messages @response)))))))

(deftest login-authenticate-success
  (rf-test/run-test-async
   (rf/dispatch-sync [::e/initialize-db])

   (let [success? (rf/subscribe [::l-s/success?])
         token (rf/subscribe [::s/token])
         params {:username "pact-verifier-user"
                 :plain "0123456789abcdef0123456789abcdef"}]

     (rf/dispatch [::l-e/login-authenticate params])
     (rf-test/wait-for [::l-e/login-authenticate-ok]
                       (is (true? @success?))
                       (is (= "your-jwt" @token))))))
