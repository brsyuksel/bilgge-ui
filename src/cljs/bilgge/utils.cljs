(ns bilgge.utils
    (:require [clojure.string :as string]
              ["jsencrypt" :as jse]))

(defn clear-rsa-key
      [priv-key]
      (-> priv-key
          (string/replace #"(?m)\n" "")
          (string/replace #"(?m)-----[A-Z\s]+-----", "")))

(defn decrypt-rsa
      [enc cipher-text]
      (if-let [plain (.decrypt enc cipher-text)]
              plain))

(defn decrypt-rsa-string-key
      [priv-key cipher-text]
      (let [enc (jse/JSEncrypt.)]
           (do
             (.setPrivateKey enc priv-key)
             (decrypt-rsa enc cipher-text))))

(defn generate-rsa-pairs
      []
      (let [enc (jse/JSEncrypt. #{:default_key_size 1024})]
           (.getKey enc)
           [(.getPrivateKey enc) (.getPublicKey enc)]))
