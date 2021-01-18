(ns bilgge.ui.messages)

(defn error-message
      [message]
      [:article.message.is-dark
       [:div.message-body message]])

(defn primary-message
      [message]
      [:article.message.is-primary
       [:div.message-body message]])
