(ns wrangler-ui.core
  (:require
   [day8.re-frame.http-fx]
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [wrangler-ui.events :as events]
   [wrangler-ui.views :as views]
   [wrangler-ui.config :as config]
   ))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root)
  (re-frame/dispatch [::events/load-projects]))
