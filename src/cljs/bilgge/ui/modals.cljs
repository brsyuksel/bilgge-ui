(ns bilgge.ui.modals
  (:require [re-frame.core :as re-frame]
            [bilgge.collections.events :as collevs]))

(defn modal-card
  [title content footer active? cancelable? on-close]
  [:div {:id "modal-card" :class ["modal" (when active? "is-active")]}
   [:div.modal-background]
   [:div.modal-card
    [:header.modal-card-head
     [:p.modal-card-title title]
     (when cancelable?
       [:button.delete {:aria-label "close"
                        :on-click #(let [modal-div (.getElementById js/document "modal-card")]
                                     (.remove (.-classList modal-div) "is-active")
                                     (when-not (nil? on-close)
                                       (on-close)))}])]
    [:section.modal-card-body content]
    [:footer.modal-card-foot.is-flex.is-justify-content-flex-end
     footer]]])
