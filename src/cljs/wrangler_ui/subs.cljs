(ns wrangler-ui.subs
  (:require
    [re-frame.core :as re-frame]))

(re-frame/reg-sub
  ::screen
  (fn [db] (:screen db)))

(re-frame/reg-sub
  ::projects
  (fn [db] (->> (:projects db)
                (vals)
                (sort-by :name))))

(re-frame/reg-sub
  ::name
  (fn [db] (get-in db [:project :name])))

(re-frame/reg-sub
  ::id
  (fn [db] (get-in db [:project :id])))

(re-frame/reg-sub
  ::edit-name?
  (fn [db] (:edit-name? db)))

(re-frame/reg-sub
  ::code
  (fn [db] (get-in db [:project :code])))

(re-frame/reg-sub
  ::result
  (fn [db] (get-in db [:project :result])))
