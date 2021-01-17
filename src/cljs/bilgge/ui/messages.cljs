(ns bilgge.ui.messages)

(defn error-message
      [message]
      [:article.message.is-dark
       [:div.message-body message]])