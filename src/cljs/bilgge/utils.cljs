(ns bilgge.utils
    (:require [clojure.string :as string]
              ["jsencrypt" :as jse]
              ["crypto-random-string" :as crs]))

(defn random-string
      [len]
      (crs #js {:length len :type "alphanumeric"}))

(defn clear-rsa-key
      [key-str]
      (-> key-str
          (string/replace #"(?m)\n" "")
          (string/replace #"(?m)-----[A-Z\s]+-----", "")))

(defn decrypt-rsa
      [enc cipher-text]
      (if-let [plain (.decrypt enc cipher-text)]
              plain))

(defn encrypt-rsa
      [enc plain-text]
      (.encrypt enc plain-text))

(defn decrypt-rsa-string-key
      [priv-key cipher-text]
      (let [enc (jse/JSEncrypt.)]
           (do
             (.setPrivateKey enc priv-key)
             (decrypt-rsa enc cipher-text))))

(defn encrypt-rsa-string-key
      [pub-key plain-text]
      (let [enc (jse/JSEncrypt.)]
           (do
             (.setPublicKey enc pub-key)
             (encrypt-rsa enc plain-text))))

(defn generate-rsa-pairs
      []
      (let [enc (jse/JSEncrypt. #{:default_key_size 1024})]
           (.getKey enc)
           [(.getPrivateKey enc) (.getPublicKey enc)]))

(defn verify-rsa-pairs
      [prv pub]
      (let [prv (clear-rsa-key prv)
            pub (clear-rsa-key pub)
            plain (random-string 16)]
           (= plain (->> plain
                         (encrypt-rsa-string-key pub)
                         (decrypt-rsa-string-key prv)))))
