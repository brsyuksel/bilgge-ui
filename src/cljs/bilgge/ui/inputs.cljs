(ns bilgge.ui.inputs
    (:require [reagent.core :as r]))

(defn labeled-large-input
      [value-atom label help-text]
      [:div.field
       [:label.label.is-large label]
       [:div.control [:input.input.is-large {:type "text"
                                             :value @value-atom
                                             :on-change #(reset! value-atom (-> % .-target .-value))}]]
       (when help-text
             [:p.help help-text])])

(defn large-input-with-placeholder
      [value-atom placeholder help-text]
      [:div.field
       [:div.control [:input.input.is-large {:type "text"
                                             :placeholder placeholder
                                             :value @value-atom
                                             :on-change #(reset! value-atom (-> % .-target .-value))}]]
       (when help-text
             [:p.help help-text])])

(defn medium-input-with-placeholder
      [placeholder value on-change]
      [:div.field
       [:div.control
        [:input.input.is-medium.has-background-white-ter {:type "text"
                                                          :defaultValue value
                                                          :on-change on-change
                                                          :placeholder placeholder}]]])

(defn medium-secret-input-with-clipboard
      [id label value]
      [:<>
       [:div.field
        [:label.label.is-large label]]
       [:div.field.has-addons.inputs-value
       [:div.control.is-expanded
        [:input.input.is-medium.has-background-white-ter {:id (str "inp-" id)
                                                          :type "password"
                                                          :defaultValue value
                                                          :readOnly true}]]
       [:div.control
        [:a.button.is-medium.has-background-white-ter {:on-click #(let [inputs-val (.getElementById js/document (str "inp-" id))]
                                                                       (set! (.-type inputs-val) "text")
                                                                       (.select inputs-val)
                                                                       (.execCommand js/document "copy")
                                                                       (set! (.-type inputs-val) "password"))}
         [:i.fas.fa-clipboard]]]]])

(defn large-input-with-placeholder-white-bis
      [value-atom placeholder help-text]
      [:div.field
       [:div.control
        [:input.input.is-large.has-background-white-bis
         {:type "text"
          :placeholder placeholder
          :value @value-atom
          :on-change #(reset! value-atom (-> % .-target .-value))}]]
       (when help-text
             [:p.help help-text])])

(defn labeled-large-file-input
      [value-atom fname-atom label help-text]
      [:div.field
       [:div.file.is-large.is-fullwidth.has-name
        [:label.file-label
         [:input.file-input {:type "file"
                             :name "file"
                             :on-change #(let [file (-> % .-target .-files first)
                                               reader (js/FileReader.)
                                               _ (set! (.-onload reader) (fn [] (reset! value-atom (.-result reader))))]
                                              (if file
                                                  (do
                                                    (.readAsText reader file)
                                                    (reset! fname-atom (.-name file)))))}]
         [:span.file-cta
          [:span.file-icon [:i.fas.fa-upload]]
          [:span.file-label label]]
         [:span.file-name @fname-atom]]]
       (when help-text
             [:p.help help-text])])

(defn labeled-large-select
      [value-atom label options]
      [:div.field
       [:label.label.is-large label]
       [:div.control
        [:div.select.is-large.is-fullwidth
         [:select {:on-change #(reset! value-atom (-> % .-target .-value))}
          (for [[v l] options]
               ^{:key v}
               [:option {:value v} l])]]]])
