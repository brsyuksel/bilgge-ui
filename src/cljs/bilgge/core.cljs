(ns bilgge.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]
   [bilgge.events :as events]
   [bilgge.views :as views]
   [bilgge.config :as config]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(def routes
  [["/" {:name :app-page}]
   ["/register" {:name :register-page}]
   ["/login" {:name :login-page}]])

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (rfe/start!
   (rf/router routes)
   (fn [m] (re-frame/dispatch [::events/set-route-name (-> m :data :name)]))
   {:use-fragment false})
  (mount-root))
