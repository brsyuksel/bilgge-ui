(ns bilgge.login.views
  (:require [clojure.string :as string]
            [reagent.core :as r]
            [re-frame.core :as re-frame]
            [bilgge.ui.inputs :as inputs]
            [bilgge.ui.messages :as messages]
            [bilgge.utils :as utils]
            [bilgge.events :as events]
            [bilgge.login.subs :as s]
            [bilgge.login.events :as e]))

(def username-atom (r/atom ""))
(def priv-key-fname-atom (r/atom ""))
(def priv-key-value-atom (r/atom ""))

(defn login-view
  []
  (let [loading? (re-frame/subscribe [::s/visibility :loading?])
        messages (re-frame/subscribe [::s/body-messages])
        errors (re-frame/subscribe [::s/error-messages])
        login-disabled? (or (string/blank? @username-atom) (string/blank? @priv-key-value-atom))]
    [:<>
     [:div.columns
      [:div.column.is-4.is-offset-4
       [inputs/labeled-large-input username-atom "Username"]]]
     [:div.columns
      [:div.column.is-4.is-offset-4
       [inputs/labeled-large-file-input priv-key-value-atom priv-key-fname-atom "Private Key" "Your private key won't be uploaded to the servers."]]]
     [:div.columns
      [:div.column.is-4.is-offset-4
       [:p "Hey, user! I am sorry to say that I cannot afford to keep bilgge project running anymore so I decided to do something-say-to-hard."]
       [:p "bilgge will be running until the end of the October, I'm kindly asking you that please back up your data in that time."]
       [:p "You can also ask me to get database dump as contains only your data if you are planning to run bilgge local or on your server."]
       [:p "Fell free to reach me through " [:span.has-text-weight-bold "brsyuksel [a-like-sign] gmail"]]
       [:p "It was a great pleasure, thank you all a lot (:"]]]
     (when (or @messages @errors)
       [:div.columns
        [:div.column.is-4.is-offset-4
         (for [[idx msg] (map-indexed vector @messages)]
           ^{:key idx}
           [messages/error-message msg])
         (for [[idx msg] (map-indexed vector @errors)]
           ^{:key (str "e-" idx)}
           [messages/error-message msg])]])
     [:div.columns
      [:div.column.is-4.is-offset-4.has-text-right
       [:button {:class ["button" "is-primary" "is-large" (when @loading? "is-loading")]
                 :disabled (or login-disabled? @loading?)
                 :on-click #(do
                              (re-frame/dispatch [::events/set-private-key (utils/clear-rsa-key @priv-key-value-atom)])
                              (re-frame/dispatch [::e/login-request {:username @username-atom}]))}
        "login."]]]]))
