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
  (fn [db] (get-in db [:project :data :name])))

(re-frame/reg-sub
  ::id
  (fn [db] (get-in db [:project :data :id])))

(re-frame/reg-sub
  ::edit-name?
  (fn [db] (:edit-name? db)))

(re-frame/reg-sub
  ::evaluating
  (fn [db] (get-in db [:project :evaluating])))

(re-frame/reg-sub
  ::code-show
  (fn [db] (get-in db [:project :show])))

(re-frame/reg-sub
  ::files
  (fn [db] (get-in db [:project :files])))

(re-frame/reg-sub
  ::result-tab
  (fn [db] (or (get-in db [:project :ux :result-tab])
               :input)))

(re-frame/reg-sub
  ::code
  (fn [db] (get-in db [:project :data :code])))

(re-frame/reg-sub
  ::result
  (fn [db] (get-in db [:project :data :result])))

(re-frame/reg-sub
  ::input
  (fn [db] (get-in db [:project :data :input])))

(re-frame/reg-sub
  ::error
  (fn [db] (get-in db [:project :data :error])))

(re-frame/reg-sub
  ::logs
  (fn [db] (get-in db [:project :data :logs])))
