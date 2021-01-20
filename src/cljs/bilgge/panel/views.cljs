(ns bilgge.panel.views
    (:require [clojure.string :as string]
              [reagent.core :as r]
              [re-frame.core :as re-frame]
              [bilgge.ui.modals :as modals]
              [bilgge.ui.inputs :as inputs]
              [bilgge.events :as events]
              [bilgge.subs :as subs]
              [bilgge.utils :as utils]
              [bilgge.collections.events :as collevs]
              [bilgge.collections.subs :as collsubs]))

(def new-collection-name (r/atom ""))
(defn create-new-collection-modal-body
      [help-text]
      [:<>
       [:div.columns
        [:div.column
         [inputs/large-input-with-placeholder new-collection-name "Collection Name" help-text]]]])

;; TODO: new collection name trim and length check.
(defn create-new-collection-modal-footer
      []
      (let [creating? @(re-frame/subscribe [::collsubs/visibility :creating?])
            plain-key @(re-frame/subscribe [::subs/plain-key])
            public-key @(re-frame/subscribe [::subs/public-key])]
           [:button {:disabled creating?
                     :class ["button" "is-primary" (when creating? "is-loading")]
                     :on-click (fn []
                                   (let [iv (utils/random-string 16)
                                         enc-name (utils/aes-encrypt plain-key iv @new-collection-name)
                                         enc-iv (utils/encrypt-rsa-string-key public-key iv)
                                         params {:name enc-name :_iv enc-iv}]
                                        (re-frame/dispatch-sync [::collevs/create-collection params])
                                        (re-frame/dispatch-sync [::collevs/display-new-collection-modal false])
                                        (reset! new-collection-name "")))}
            "save."]))

(defn create-new-collection-modal
      [help-text active? cancelable?]
      (modals/modal-card "Add new collection."
                         (r/as-element (create-new-collection-modal-body help-text))
                         (r/as-element (create-new-collection-modal-footer))
                         active?
                         cancelable?
                         #(re-frame/dispatch-sync [::collevs/display-new-collection-modal false])))

(defn collection-selector
      []
      (let [collections-raw @(re-frame/subscribe [::collsubs/data])
            priv-key @(re-frame/subscribe [::subs/private-key])
            plain-key @(re-frame/subscribe [::subs/plain-key])
            rsa-decrypt #(utils/decrypt-rsa-string-key priv-key %)
            aes-decrypt (fn [name iv]
                            (let [plain-iv (rsa-decrypt iv)]
                                 (utils/aes-decrypt plain-key plain-iv name)))]
           [:div.control.has-icons-left
            [:div.select.is-fullwidth
             [:select
              (for [coll collections-raw]
                   ^{:key (:id coll)}
                   [:option (aes-decrypt (:name coll) (:_iv coll))])]]
            [:span.icon.is-left [:i.fas.fa-folder]]]))

(defn search
      []
      [:div.column.is-half.is-offset-one-quarter
       [:div.control.has-icons-right
        [:input.input {:placeholder "keyword", :type "search"}]
        [:span.icon.is-right [:i.fas.fa-search]]]])

(defn content-actions
      []
      [:<>
       [:button.button.is-light.is-danger
        [:span.icon.is-large [:i.fas.fa-trash]]]
       [:button.button.is-light [:span.icon.is-large [:i.fas.fa-pen]]]
       [:button.button.is-outlined "discard"]
       [:button.button.is-primary "save."]])

(defn content-header
      []
      [:div.columns
       [:div.column.is-one-quarter
        [collection-selector]]
       [:div.column
        [:div.columns
         [search]]]
       [:div.column.is-one-quarter.has-text-right.content-controls
        [content-actions]]])

(defn side-bar-pagination
      []
      [:div.columns.is-1
       [:div.column.is-one-fifth
        [:button.button.is-light.is-small.is-fullwidth
         [:span.icon [:i.fas.fa-angle-double-left]]]]
       [:div.column.is-flex.is-flex-direction-row.is-justify-content-center.is-align-items-center
        [:h2 "0-10"]]
       [:div.column.is-one-fifth
        [:button.button.is-light.is-small.is-fullwidth
         [:span.icon [:i.fas.fa-angle-double-right]]]]])

(defn secret-item
      [type name created-at active?]
      [:div {:class ["column" "is-clickable" "is-full" "listing-item" (when active? "active")]}
       [:div.columns
        [:div.column.is-one-fifth.is-flex.is-flex-direction-row.is-align-items-center.is-justify-content-center.has-text-grey
         [:span.icon.is-large
          (if (= type "note")
              [:i.fas.fa-sticky-note]
              [:i.fas.fa-keyboard])]]
        [:div {:class ["column" (if active? "has-text-grey-dark" "has-text-grey")]}
         [:h3.is-size-4 name]
         [:p.help created-at]]]])

(defn side-bar
      []
      [:div.column.is-one-quarter
       [side-bar-pagination]
       [:div.columns.is-gapless.is-multiline
        [secret-item "note" "sticky note" "2021/03/01 16:45" false]
        [secret-item "inputs" "my passwords" "2021/03/01 16:45" true]
        [secret-item "inputs" "credit card" "2021/03/01 16:45" false]]])

(defn content-wrapper
      []
      (let [collections (re-frame/subscribe [::collsubs/data])
            display-modal? (re-frame/subscribe [::collsubs/visibility :display-modal?])
            new-coll-msg (if (= 0 (count @collections))
                             "You need to have one collection at least in order to use bilgge."
                             "Collection names are encrypted, too!")
            display? (or (= 0 (count @collections)) @display-modal?)
            cancelable? (> (count @collections) 0)]
           [:div.columns
            [create-new-collection-modal new-coll-msg display? cancelable?]
            [side-bar]
            [:div.column]]))

(defn panel-view
      []
      (r/create-class {:component-did-mount (fn []
                                                (let [token (re-frame/subscribe [::subs/token])]
                                                     (when-not @token
                                                               (re-frame/dispatch [::events/push-state :login-page]))
                                                     (re-frame/dispatch [::collevs/get-collections])))
                       :render (fn []
                                   [:<>
                                    [content-header]
                                    [content-wrapper]])}))
