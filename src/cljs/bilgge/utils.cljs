(ns bilgge.utils
  (:require [clojure.string :as string]
            ["jsencrypt" :as jse]
            ["crypto-random-string" :as crs]
            ["aes-js" :as aes]
            ["js-sha256" :as js-sha]))

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
    (.setPrivateKey enc priv-key)
    (decrypt-rsa enc cipher-text)))

(defn encrypt-rsa-string-key
  [pub-key plain-text]
  (let [enc (jse/JSEncrypt.)]
    (.setPublicKey enc pub-key)
    (encrypt-rsa enc plain-text)))

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

(defn arr->16x-length-arr
  [arr]
  (let [cnt (.-length arr)
        remain (- 16 (mod cnt 16))
        new-cnt (+ cnt remain)
        new-arr (js/Uint8Array. new-cnt)]
    (.set new-arr arr)
    new-arr))

(defn padded-arr->arr
  [arr]
  (let [l (last arr)
        r (butlast arr)]
    (if (= l 0)
      (recur r)
      (clj->js arr))))

(defn aes-encrypt
  [key iv plain]
  (let [to-byte (.. aes -utils -utf8 -toBytes)
        to-hex (.. aes -utils -hex -fromBytes)
        key-byte (to-byte key)
        iv-byte (to-byte iv)
        plain-byte (to-byte plain)
        plain-byte (arr->16x-length-arr plain-byte)
        cbc-enc-constructor (.. aes -ModeOfOperation -cbc)
        cbc-enc (cbc-enc-constructor. key-byte iv-byte)]
    (-> cbc-enc
        (.encrypt plain-byte)
        to-hex)))

(defn aes-decrypt
  [key iv hex]
  (let [str-to-byte (.. aes -utils -utf8 -toBytes)
        bytes-to-str (.. aes -utils -utf8 -fromBytes)
        hex-to-byte (.. aes -utils -hex -toBytes)
        encrypted-bytes (hex-to-byte hex)
        key-byte (str-to-byte key)
        iv-byte (str-to-byte iv)
        cbc-enc-constructor (.. aes -ModeOfOperation -cbc)
        cbc-enc (cbc-enc-constructor. key-byte iv-byte)]
    (-> cbc-enc
        (.decrypt encrypted-bytes)
        padded-arr->arr
        bytes-to-str)))

(defn sha256
  [text]
  (js-sha text))
