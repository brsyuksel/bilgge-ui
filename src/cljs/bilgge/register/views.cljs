(ns bilgge.register.views
    (:require [clojure.string :as string]
              [reagent.core :as r]
              [goog.crypt.base64 :as b64]
              ["crypto-random-string" :as crs]
              [bilgge.utils :as utils]
              [bilgge.ui.inputs :as inputs]))

(def register-current-step (r/atom "i"))
(def register-username (r/atom ""))
(def register-key-method-selection (r/atom ""))
(def register-key-method (r/atom ""))
(def register-key-priv-fname (r/atom ""))
(def register-key-priv-value (r/atom ""))
(def register-key-pub-fname (r/atom ""))
(def register-key-pub-value (r/atom ""))

(defn register-intro-view
      []
      [:<>
       [:div.columns
        [:div.column.is-4.is-offset-4
         [:div.block.is-size-4 "This wizard will help you create your bilgge account."]
         [:div.block.is-size-3 "Let's start with choosing a username."]]]
       [:div.columns
        [:div.column.is-4.is-offset-4.has-text-right
         [:button.button.is-primary.is-large {:on-click #(reset! register-current-step "u")} "start."]]]])

(defn register-username-view
      []
      [:<>
       [:div.columns
        [:div.column.is-4.is-offset-4
         [inputs/labeled-large-input register-username "Username" "The only public thing about you in bilgge is your username."]]]
       [:div.columns
        [:div.column.is-4.is-offset-4.has-text-right
         [:button.button.is-primary.is-large {:disabled (= "" @register-username)
                                              :on-click #(reset! register-current-step "k")} "next."]]]])

(defn register-key-selection-view
      []
      [:<>
       [:div.columns
        [:div.column.is-4.is-offset-4
         [:div.block.is-size-3 "Passwords cannot protect you."]
         [:div.block.is-size-4
          [:p "Instead, we will use key pairs!"]
          [:a.help.has-text-grey-dark
           [:u "Learn how they work"]]]]]
       [:div.columns
        [:div.column.is-4.is-offset-4
         [inputs/labeled-large-select register-key-method-selection "I want" {"" "..."
                                                                              "own" "to use my own keys"
                                                                              "generate" "bilgge to generate for me"}]]]
       [:div.columns
        [:div.column.is-4.is-offset-4.has-text-right
         [:button.button.is-primary.is-large {:disabled (= "" @register-key-method-selection)
                                              :on-click #(reset! register-key-method @register-key-method-selection)}
          "next."]]]])

(defn register-key-own-view
      []
      [:<>
       [:div.columns
        [:div.column.is-4.is-offset-4
         [:div.block.is-size-3
          [:p "Please select your keys."]
          [:p.help.has-text-grey-dark
           "bilgge "
           [:strong "can't"]
           " work with password protected keys."]]]]
       [:div.columns
        [:div.column.is-4.is-offset-4
         [inputs/labeled-large-file-input register-key-pub-value register-key-pub-fname "Public Key" "Your public key will be uploaded to the servers."]]]
       [:div.columns
        [:div.column.is-4.is-offset-4
         [inputs/labeled-large-file-input register-key-priv-value register-key-priv-fname "Private Key" "Your private key won't be uploaded to the servers."]
         [:p [:strong "We will verify your pairs by using your private key in your browser before creating your account."]]]]
       [:div.columns
        [:div.column.is-4.is-offset-4.has-text-right
         [:button.button.is-outlined.is-large {:style {:margin-right "3px"}
                                               :on-click #(do
                                                            (reset! register-key-pub-value "")
                                                            (reset! register-key-pub-fname "")
                                                            (reset! register-key-priv-value "")
                                                            (reset! register-key-priv-fname "")
                                                            (reset! register-key-method ""))} "clear"]
         [:button.button.is-warning.is-large {:disabled (or (string/blank? @register-key-pub-value) (string/blank? @register-key-priv-value))
                                              :on-click #(reset! register-current-step "d")}
          "next."]]]])

(defn register-key-generate-view
      []
      (let [[priv-key pub-key] (utils/generate-rsa-pairs)
            _ (reset! register-key-pub-value (utils/clear-rsa-key pub-key))
            hrefy #(str "data:text/plain;base64," (b64/encodeString %))]
           [:<>
            [:div.columns
             [:div.column.is-4.is-offset-4
              [:div.block.is-size-3
               [:p "Download your generated keys."]
               [:p.help.has-text-grey-dark
                "We strongly suggest "
                [:strong "not to use"]
                " your keys outside of bilgge."]]]]
            [:div.columns
             [:div.column.is-4.is-offset-4
              [:div.field
               [:a.button.is-large.is-light.is-fullwidth {:href (hrefy pub-key)
                                                          :download "bilgge.pub"}
                [:span.icon.is-large [:i.fas.fa-download]]
                [:span "Public Key"]]]]]
            [:div.columns
             [:div.column.is-4.is-offset-4
              [:div.field
               [:a.button.is-large.is-light.is-danger.is-fullwidth {:href (hrefy priv-key)
                                                                    :download "bilgge.priv"}
                [:span.icon.is-large [:i.fas.fa-download]]
                [:span "Private Key"]]]
              [:p.help
               "Keep your private key file away from evil eyes. "
               [:i "(and never lose it!)"]]]]
            [:div.columns
             [:div.column.is-4.is-offset-4.has-text-right
              [:button.button.is-outlined.is-large {:style {:margin-right "3px"}
                                                    :on-click #(do
                                                                 (reset! register-key-pub-value "")
                                                                 (reset! register-key-method ""))} "clear"]
              [:button.button.is-warning.is-large {:on-click #(reset! register-current-step "d")} "next."]]]]))

(defn register-key-view
      []
      (case @register-key-method
            "own" [register-key-own-view]
            "generate" [register-key-generate-view]
            [register-key-selection-view]))

(defn register-donation-view
     []
     [:<>
      [:div.columns
       [:div.column.is-4.is-offset-4
        [:div.block.is-size-3 "Pay as much as you want!"]
        [:div.block.is-size-5
         [:p
          "bilgge is totally free, you "
          [:u "don't"]
          " have to make payment if you don't want."]]]]
      [:div.columns
       [:div.column.is-4.is-offset-4
        [:div.field
         [:button.button.is-large.is-light.is-fullwidth
          [:span.icon.is-large [:i.fab.fa-patreon]]
          [:span "Patreon"]]]]]
      [:div.columns
       [:div.column.is-4.is-offset-4
        [:div.field.has-addons
         [:p.control
          [:span.select.is-large
           [:select
            {:name "coin"}
            [:option {:value "btc"} "btc"]
            [:option {:value "eth"} "eth"]
            [:option {:value "xmr"} "xmr"]
            [:option {:value "eos"} "eos"]
            [:option {:value "xrp"} "xrp"]]]]
         [:p.control.is-expanded
          [:input.input.is-large
           {:readonly "readonly",
            :value "coin-wallet-address",
            :type "text"}]]
         [:p.control [:a.button.is-large [:i.fas.fa-clipboard]]]]]]
      [:div.columns
       [:div.column.is-4.is-offset-4.has-text-right
        [:button.button.is-primary.is-large "register."]]]])

(defn register-steps
      []
      [:div.columns
       [:div.column.is-4.is-offset-4
        [:div.columns
         [:div.column.has-text-centered
          [:span {:class ["register-step" "is-clickable" "p-2" "is-size-3" (when (= @register-current-step "u") "active")]
                  :on-click #(reset! register-current-step "u")}
           "u."]]
         [:div.column.has-text-centered
          [:span {:class ["register-step" "is-clickable" "p-2" "is-size-3" (when (= @register-current-step "k") "active")]
                  :on-click #(when-not (string/blank? @register-username)
                                   (reset! register-current-step "k"))}
           "k."]]
         [:div.column.has-text-centered
          [:span {:class ["register-step" "is-clickable" "p-2" "is-size-3" (when (= @register-current-step "d") "active")]
                  :on-click #(case @register-key-method
                                   "generate" (when-not (string/blank? @register-key-pub-value)
                                                        (reset! register-current-step "d"))
                                   "own" (when-not (or (string/blank? @register-key-pub-value) (string/blank? @register-key-priv-value))
                                                   (reset! register-current-step "d")))}
           "d."]]]]])

(defn register-view
      []
      [:<>
       [register-steps]
       (case @register-current-step
             "i" [register-intro-view]
             "u" [register-username-view]
             "k" [register-key-view]
             "d" [register-donation-view]
             [register-intro-view])])
