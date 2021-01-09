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

(def test-public-key
  "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCqtR6Mb/T3BlMBMn7iqNAE01wXBYVtnA4qiPkaUEkRrAlNLHmhQ5A1lYzKfykd2cYv1VKbzKQ1RzFzkEOKfokVM3ZWpIP6GSi+h9UX5p6FHzVOr3qN7wDmcMOIRYPHr81k/IS61ezIahwpisoQjWuJQWJr27y68HIQH+9Fr7QqEQIDAQAB")
(def test-key
  "Zc/RUJ0hGphl+T/EQ34x6oLsxur8WTjP4a2nM7fztnatxJXwM6ZVwkHK//WfChlLkWEAKLQ8BVMcHI82O96oLY+ujM+b79Go4xMUM1TC5W1zT5RXCX3Fh7hZYIcsbFC26ftLgBkDMOZemfNv1vFgOCjSVVNZf1IGWblREfL3N8Y=")
(def test-salt
  "CTTePGjU/5LJr2RzQ1uAqUUyvK7UrTjF8lmE59vWsNR3vNrxSDS+1qBqblDVZEiuWOAaPi5TkymLkWR5HpDvRRY5ceMF0BlHByWqkBbAaD3lKZv1W0HTROppygdgRi9LbL0GkZZ7uiMptq9iXl5hOrjb/2OUGnm4NgF4gcWx1JE=")
(def test-cipher
  "LyywH59U0sRaO/T/V/ZpCqntgO0Wk4XHqDanePMzTqKjnTUre0Cl2urc7ioQVsCVKGanuQGqdSszGvTc0NMeFtw1SVCx1touP2a9KKWZ8cUW2m2rJY00+gXx28zHpbDcZ3I3cq444JimoveT505+l5lixwNKfLxdMogeAxur590=")
;; ---------------------------------------------------------------------------------------

(def register-success
  (clj->js {:uponReceiving "success post /register"
            :withRequest {:method "POST"
                          :path "/register"
                          :headers {"Content-Type" "application/json"}
                          :body {:username "ybaroj"
                                 :public_key test-public-key
                                 :key test-key
                                 :salt test-salt}}
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
                              :body {:cipher test-cipher}}}))

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
                              :body {:token "your-jwt"}}}))

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
                  (.addInteraction provider login-authenticate-success))))
    (.on process "SIGINT" #(do
                             (.finalize provider)
                             (.removeAllServers pact)))))

(defn pact-publish
  []
  (prn "publisher"))

(defn main
  [& args]
  (case (first args)
    "server" (pact-server)
    "publish" (pact-publish)))
