(ns bilgge.views
  (:require [re-frame.core :as re-frame]
            [bilgge.subs :as subs]
            [bilgge.collections.events :as collevs]
            [bilgge.secrets.events :as secevs]
            [bilgge.login.views :as login.v]
            [bilgge.register.views :as register.v]
            [bilgge.panel.views :as panel.v]))

(defn header-add-new-item-menu
      [display?]
      [:div.column.is-one-fifth
       (when display?
             [:div.create-new-wrapper
        [:div.dropdown.is-hoverable
         [:div.dropdown-trigger
          [:button.button.is-light.is-large {:aria-haspopup "true" :aria-controls "create-new-menu"}
           [:span.icon.is-small
            [:i.fas.fa-plus {:aria-hidden "true"}]]]]
         [:div#create-new-menu.dropdown-menu {:role "menu"}
          [:div.dropdown-content
           [:div.dropdown-item.is-clickable {:on-click #(do
                                                          (reset! panel.v/secret-note-title "")
                                                          (reset! panel.v/secret-note-content "")
                                                          (re-frame/dispatch [::secevs/display-editor :note]))}
            [:span.icon
             [:i.fas.fa-sticky-note]]
            [:span.is-size-5 "note"]]
           [:div.dropdown-item.is-clickable {:on-click #(do
                                                          (reset! panel.v/secret-note-title "")
                                                          (reset! panel.v/secret-note-content [{:id (str (random-uuid)) :key "" :value ""}])
                                                          (re-frame/dispatch [::secevs/display-editor :inputs]))}
            [:span.icon
             [:i.fas.fa-keyboard]]
            [:span.is-size-5 "inputs"]]
           [:hr.dropdown-divider]
           [:div.dropdown-item.is-clickable {:on-click #(re-frame/dispatch [::collevs/display-new-collection-modal true])}
            [:span.icon
             [:i.fas.fa-folder]]
            [:span.is-size-5 "collection"]]]]]])])

(defn header-brand
      []
      [:div.column.is-one-fifth.is-offset-one-fifth
       [:h1.is-size-1.has-text-centered.has-text-grey-dark.brand-text "bilgge."]])

(defn header-user-actions
      [display?]
      [:div.column.is-one-fifth.is-offset-one-fifth
       (when display?
             [:div.management-actions
        [:button.button.is-white.is-large.has-text-grey {:on-click #(set! (.. js/window -location -href) "/login")}
         [:span.icon.is-large
          [:i.fas.fa-sign-out-alt]]]])])

(defn header
      [logged-in?]
      [:section.section.section-header
       [:div.container
         [:div.columns
          [header-add-new-item-menu logged-in?]
          [header-brand]
          [header-user-actions logged-in?]]]])

(defn main-panel
      []
      (let [route-name @(re-frame/subscribe [::subs/route-name])
            display-app-menu? (= route-name :app-page)]
           [:<>
            [header display-app-menu?]
            [:section.section
             [:div.container
              (case route-name
                    :login-page [login.v/login-view]
                    :register-page [register.v/register-view]
                    :app-page [panel.v/panel-view]
                    [login.v/login-view])]]]))
