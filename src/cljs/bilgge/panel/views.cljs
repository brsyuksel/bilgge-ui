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
              [bilgge.collections.subs :as collsubs]
              [bilgge.secrets.events :as secevs]
              [bilgge.secrets.subs :as secsubs]))

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

(def selected-collection (r/atom nil))
(def offset (r/atom 0))
(def limit (r/atom 10))
(def search-hashes (r/atom ""))
(defn get-collections
      []
      (let [params {:collection_id @selected-collection
                    :offset @offset
                    :limit @limit
                    :q @search-hashes}]
           (re-frame/dispatch [::secevs/get-secrets params])))

(defn collection-selector
      []
      (let [collections-raw @(re-frame/subscribe [::collsubs/data])
            priv-key @(re-frame/subscribe [::subs/private-key])
            plain-key @(re-frame/subscribe [::subs/plain-key])
            rsa-decrypt #(utils/decrypt-rsa-string-key priv-key %)
            aes-decrypt (fn [name iv]
                            (let [plain-iv (rsa-decrypt iv)]
                                 (utils/aes-decrypt plain-key plain-iv name)))
            _ (when-not @selected-collection
                        (do
                          (reset! selected-collection (-> collections-raw first :id))
                          (get-collections)))]
           [:div.control.has-icons-left
            [:div.select.is-fullwidth
             [:select {:on-change #(do
                                     (reset! selected-collection (-> % .-target .-value))
                                     (reset! offset 0)
                                     (reset! limit 10)
                                     (get-collections))}
              (for [coll collections-raw]
                   ^{:key (:id coll)}
                   [:option {:value (:id coll)
                             :selected (= @selected-collection (:id coll))}
                    (aes-decrypt (:name coll) (:_iv coll))])]]
            [:span.icon.is-left [:i.fas.fa-folder]]]))

(defn search
      []
      [:div.column.is-half.is-offset-one-quarter
       [:div.control.has-icons-right
        [:input.input {:placeholder "keyword", :type "search"}]
        [:span.icon.is-right [:i.fas.fa-search]]]])

(def secret-note-title (r/atom ""))
(def secret-note-content (r/atom ""))
(defn send-note-form
      []
      (let [editing? @(re-frame/subscribe [::secsubs/visibility :editing?])
            selected-id @(re-frame/subscribe [::secsubs/selected-id])
            pub-key @(re-frame/subscribe [::subs/public-key])
            plain-key @(re-frame/subscribe [::subs/plain-key])
            hash-salt @(re-frame/subscribe [::subs/plain-salt])
            plain-iv (utils/random-string 16)
            enc-iv (utils/encrypt-rsa-string-key pub-key plain-iv)
            enc-type (utils/aes-encrypt plain-key plain-iv "note")
            enc-title (utils/aes-encrypt plain-key plain-iv @secret-note-title)
            enc-body (utils/aes-encrypt plain-key plain-iv @secret-note-content)
            hashes (->> (string/split @secret-note-title #"\s")
                        (map #(str % hash-salt))
                        (map utils/sha256))
            params {:collection_id @selected-collection
                    :type enc-type
                    :title enc-title
                    :content enc-body
                    :_iv enc-iv
                    :hashes hashes}]
           (if-not editing?
                   (re-frame/dispatch [::secevs/create-secret params])
                   (re-frame/dispatch [::secevs/edit-secret @selected-collection selected-id params]))
           (reset! secret-note-title "")
           (reset! secret-note-content "")))
(defn content-actions
      []
      (let [display-editor? (re-frame/subscribe [::secsubs/visibility :display-editor?])
            selected-id (re-frame/subscribe [::secsubs/selected-id])]
           (cond
             @display-editor? [:<>
                               [:button.button.is-outlined {:on-click #(do
                                                                         (reset! secret-note-title "")
                                                                         (reset! secret-note-content "")
                                                                         (re-frame/dispatch [::secevs/display-editor false])
                                                                         (re-frame/dispatch [::secevs/editing? false]))} "discard"]
                               [:button.button.is-primary {:on-click #(do
                                                                        (send-note-form))} "save."]]
             @selected-id [:<>
                           [:button.button.is-light.is-danger {:on-click #(re-frame/dispatch [::secevs/delete-secret @selected-collection @selected-id])}
                            [:span.icon.is-large [:i.fas.fa-trash]]]
                           [:button.button.is-light {:on-click #(do
                                                                  (re-frame/dispatch [::secevs/editing? true])
                                                                  (re-frame/dispatch [::secevs/display-editor true]))}
                            [:span.icon.is-large [:i.fas.fa-pen]]]])))

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
        [:button.button.is-light.is-small.is-fullwidth {:disabled (= @offset 0)
                                                        :on-click #(do
                                                                     (swap! offset - 10)
                                                                     (swap! limit - 10)
                                                                     (get-collections))}
         [:span.icon [:i.fas.fa-angle-double-left]]]]
       [:div.column.is-flex.is-flex-direction-row.is-justify-content-center.is-align-items-center
        [:h2 (str @offset "-" @limit)]]
       [:div.column.is-one-fifth
        [:button.button.is-light.is-small.is-fullwidth {:on-click #(do
                                                                     (swap! offset + 10)
                                                                     (swap! limit + 10)
                                                                     (get-collections))}
         [:span.icon [:i.fas.fa-angle-double-right]]]]])

(defn secret-item
      [id type name created-at active?]
      [:div {:class ["column" "is-clickable" "is-full" "listing-item" (when active? "active")]
             :on-click #(do
                          (re-frame/dispatch [::secevs/select-secret id])
                          (re-frame/dispatch [::secevs/get-secret-detail id]))}
       [:div.columns
        [:div.column.is-one-fifth.is-flex.is-flex-direction-row.is-align-items-center.is-justify-content-center.has-text-grey
         [:span.icon.is-large
          (if (= type "note")
              [:i.fas.fa-sticky-note]
              [:i.fas.fa-keyboard])]]
        [:div {:class ["column" (if active? "has-text-grey-dark" "has-text-grey")]}
         [:h3.is-size-4 name]
         [:p.help created-at]]]])

(defn listing-secrets
      []
      (let [secrets @(re-frame/subscribe [::secsubs/data])
            selected-id @(re-frame/subscribe [::secsubs/selected-id])
            priv-key @(re-frame/subscribe [::subs/private-key])
            plain-key @(re-frame/subscribe [::subs/plain-key])
            rsa-decrypt #(utils/decrypt-rsa-string-key priv-key %)]
           (if (> (count secrets) 0) [:div.columns.is-gapless.is-multiline
            (for [secret secrets]
                 (let [iv (:_iv secret)
                       plain-iv (rsa-decrypt iv)
                       s-type (utils/aes-decrypt plain-key plain-iv (:type secret))
                       title (utils/aes-decrypt plain-key plain-iv (:title secret))
                       created-at (:created_at secret)]
                      ^{:key (:id secret)}
                      [secret-item (:id secret) s-type title created-at (= selected-id (:id secret))]))]
               [:div.columns "empty list"])))

(defn side-bar
      []
      [:div.column.is-one-quarter
       [side-bar-pagination]
       [listing-secrets]])

(defn note-form
      []
      [:div.column
       [:div.content.p-3
        [:div.columns
         [:div.column
          [:div.field
           [:div.control
            [inputs/large-input-with-placeholder-white-bis secret-note-title "Title" nil]]]]]
        [:div.columns.is-flex.is-justify-content-center
         [:div#editor-toolbar.column.is-flex.is-half.is-justify-content-space-between.is-align-items-center
          [:button.button.is-medium
           {:data-trix-attribute "bold"}
           [:span.icon.is-large [:i.fas.fa-bold]]]
          [:button.button.is-medium
           {:data-trix-attribute "italic"}
           [:span.icon.is-large [:i.fas.fa-italic]]]
          [:button.button.is-medium
           {:data-trix-attribute "strike"}
           [:span.icon.is-large [:i.fas.fa-strikethrough]]]
          [:button.button.is-medium
           {:data-trix-attribute "heading1"}
           [:span.icon.is-large [:i.fas.fa-heading]]]
          [:button.button.is-medium
           {:data-trix-attribute "quote"}
           [:span.icon.is-large [:i.fas.fa-quote-left]]]
          [:button.button.is-medium
           {:data-trix-attribute "code"}
           [:span.icon.is-large [:i.fas.fa-code]]]
          [:button.button.is-medium
           {:data-trix-attribute "bullet"}
           [:span.icon.is-large [:i.fas.fa-list]]]
          [:button.button.is-medium
           {:data-trix-attribute "number"}
           [:span.icon.is-large [:i.fas.fa-list-ol]]]]]
        [:div.columns
         [:div.column
          [:input#note-body {:type "hidden" :value @secret-note-content}]
          [:trix-editor.text-editor
           {:contenteditable "",
            :role "textbox",
            :input "note-body",
            :placeholder "Note down your secrets here.",
            :toolbar "editor-toolbar"}]]]]])

(defn secret-content
      []
      (if-let [detail @(re-frame/subscribe [::secsubs/detail])]
              (let [priv-key @(re-frame/subscribe [::subs/private-key])
                    plain-key @(re-frame/subscribe [::subs/plain-key])
                    plain-iv (utils/decrypt-rsa-string-key priv-key (:_iv detail))
                    plain-title (utils/aes-decrypt plain-key plain-iv (:title detail))
                    plain-content (utils/aes-decrypt plain-key plain-iv (:content detail))
                    _ (reset! secret-note-title plain-title)
                    _ (reset! secret-note-content plain-content)]
                   [:div.column
                    [:div.content.p-3
                     [:div.columns.content-title
                      [:div.column
                       [:h2 plain-title]
                       [:div.divider.is-right (str "Last Update at " (:updated_at detail))]]]
                     [:div.columns
                      [:div.column {:dangerouslySetInnerHTML {:__html plain-content}}]]]])
              [:h3 "EMPTY SCREEN"]))

(defn content-wrapper
      []
      (let [collections (re-frame/subscribe [::collsubs/data])
            display-modal? (re-frame/subscribe [::collsubs/visibility :display-modal?])
            display-editor? (re-frame/subscribe [::secsubs/visibility :display-editor?])
            new-coll-msg (if (= 0 (count @collections))
                             "You need to have one collection at least in order to use bilgge."
                             "Collection names are encrypted, too!")
            display? (or (= 0 (count @collections)) @display-modal?)
            cancelable? (> (count @collections) 0)]
           [:div.columns
            [create-new-collection-modal new-coll-msg display? cancelable?]
            [side-bar]
            (if @display-editor?
                [note-form]
                [secret-content])]))

(defn panel-view
      []
      (r/create-class {:component-did-mount (fn []
                                                (let [token (re-frame/subscribe [::subs/token])]
                                                     (when-not @token
                                                               (re-frame/dispatch [::events/push-state :login-page]))
                                                     (re-frame/dispatch [::collevs/get-collections])
                                                     (.addEventListener js/window "trix-change" #(let [note-input (.getElementById js/document "note-body")
                                                                                                       note-plain (.-value note-input)]
                                                                                                      (reset! secret-note-content note-plain)))))
                       :render (fn []
                                   [:<>
                                    [content-header]
                                    [content-wrapper]])}))
