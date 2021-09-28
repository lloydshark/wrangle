(ns wrangler-ui.events
  (:require
    [ajax.core :as ajax]
    [re-frame.core :as re-frame]
    [wrangler-ui.db :as db]
    ))

(re-frame/reg-event-db
  ::initialize-db
  (fn [_ _]
    db/default-db))

(re-frame/reg-event-fx
  ::evaluate
  (fn [{:keys [db]} _]                                      ;; the first param will be "world"
    {:db         (assoc-in db [:project :evaluating] true)
     :http-xhrio {:method          :post
                  :params          (-> db
                                       :project
                                       (select-keys [:id :code]))
                  :uri             "http://localhost:8081/evaluate"
                  :timeout         10000
                  :format          (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format {})
                  ;:response-format (ajax/json-response-format {:keywords? true}) ;; IMPORTANT!: You must provide this.
                  :on-success      [::evaluate-success]
                  :on-failure      [::evaluate-failure]}}))

(re-frame/reg-event-db
  ::evaluate-success
  (fn [db event]
    (println "evaluate-success")
    (println event)
    (-> db
        (assoc-in [:project :evaluating] false)
        (assoc-in [:project :result] (-> event
                                         (second)
                                         (get :result))))))

(re-frame/reg-event-db
  ::evaluate-failure
  (fn [db event]
    (println "evaluate-failed")
    (println event)
    (assoc-in db [:project :evaluating] false)))

(re-frame/reg-event-db
  ::edit-code
  (fn [db [_ code]]
    (assoc-in db [:project :code] code)))

(re-frame/reg-event-fx
  ::load-projects
  (fn [_ _]
    {:http-xhrio {:method          :get
                  :uri             "http://localhost:8081/projects"
                  :timeout         10000                    ;; optional see API docs
                  :response-format (ajax/transit-response-format {}) ;; IMPORTANT!: You must provide this.
                  :on-success      [::projects-success]
                  :on-failure      [::projects-failure]}}))

(re-frame/reg-event-db
  ::projects-success
  (fn [db [_ projects]]
    (println "projects success")
    (assoc db :projects projects)))

(re-frame/reg-event-db
  ::projects-failure
  (fn [db event]
    (println "evaluate-failed")
    (println event)
    db))

(re-frame/reg-event-fx
  ::load-project
  (fn [{:keys [db]} [_ project-id]]
    {:db         (assoc db :screen :project-loading)
     :http-xhrio {:method          :get
                  :uri             (str "http://localhost:8081/projects/" project-id)
                  :timeout         10000                    ;; optional see API docs
                  :response-format (ajax/transit-response-format {}) ;; IMPORTANT!: You must provide this.
                  :on-success      [::load-project-success]
                  :on-failure      [::load-project-failure]}}))

(re-frame/reg-event-db
  ::load-project-success
  (fn [db [_ project]]
    (assoc db :screen :project
              :project project)))

(re-frame/reg-event-db
  ::close-project
  (fn [db _]
    (-> db
        (assoc :screen :home)
        (dissoc :project))))

(re-frame/reg-event-db
  ::new-project
  (fn [db _]
    (-> db
        (assoc :screen :project
               :project {:name "New Thing"
                         :code "(+ 1 2)"}))))

(re-frame/reg-event-fx
  ::save-project
  (fn [{:keys [db]} _]
    {:http-xhrio {:method          :post
                  :params          (get-in db [:project])
                  :uri             "http://localhost:8081/projects"
                  :timeout         10000
                  :format          (ajax/transit-request-format)
                  :response-format (ajax/transit-response-format {})
                  :on-success      [::save-project-success]
                  :on-failure      [::save-project-failure]}}))

(re-frame/reg-event-db
  ::save-project-success
  (fn [db [_ saved-project]]
    (-> db
        (assoc :project saved-project)
        (assoc-in [:projects (:id saved-project)]
                  (select-keys saved-project [:id :name])))))

(re-frame/reg-event-db
  ::save-project-failure
  (fn [db event]
    (println event)
    db))

(re-frame/reg-event-db
  ::edit-name-start
  (fn [db _]
    (assoc db :edit-name? true)))

(re-frame/reg-event-fx
  ::edit-name-end
  (fn [{:keys [db]} _]
    {:db (dissoc db :edit-name?)
     :fx [[:dispatch [::save-project]]]}))

(re-frame/reg-event-db
  ::edit-name
  (fn [db [_ updated-name]]
    (assoc-in db [:project :name] updated-name)))

(re-frame/reg-event-fx
  ::delete-project
  (fn [_ [_ project-id]]
    {:confirm {:message "Delete this Project?"
               :event   [::delete-project-confirmed project-id]}}))

(re-frame/reg-event-fx
  ::delete-project-confirmed
  (fn [{:keys [db]} [_ project-id]]
    {:db         (-> db
                     (assoc :screen :home)
                     (dissoc :project)
                     (update-in [:projects] dissoc project-id))
     :http-xhrio {:method          :delete
                  :uri             (str "http://localhost:8081/projects/" project-id)
                  :timeout         10000
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [::delete-project-success]
                  :on-failure      [::delete-project-failure]}}))

(re-frame/reg-event-fx
  ::delete-project-success
  (fn [_ _]
    (println ::delete-project-success)
    {}))

(re-frame/reg-event-fx
  ::delete-project-failure
  (fn [_ _]
    (println ::delete-project-failure)
    {}))

(re-frame/reg-fx
  :confirm
  (fn [{:keys [event message]}]
    (when (js/confirm message)
      (re-frame/dispatch event))))

(re-frame/reg-event-fx
  ::just-log
  (fn [_ event]
    (println event)
    {}))

(re-frame/reg-event-fx
  ::open-files-folder
  (fn [{:keys [db]} _]
    (let [project-id (get-in db [:project :id])]
      {:http-xhrio {:method          :get
                    :uri             (str "http://localhost:8081/projects/" project-id "/file-folder")
                    :timeout         10000
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-failure      [::just-log "open-files-folder-failure"]
                    :on-success      [::just-log "open-files-folder-success"]}})))

(re-frame/reg-event-fx
  ::list-files
  (fn [{:keys [db]} _]
    (let [project-id (get-in db [:project :id])]
      {:db         (assoc-in db [:project :show] :files)
       :http-xhrio {:method          :get
                    :uri             (str "http://localhost:8081/projects/" project-id "/files")
                    :timeout         10000
                    :format          (ajax/transit-request-format)
                    :response-format (ajax/transit-response-format {})
                    :on-failure      [::just-log "list-files-failure"]
                    :on-success      [::list-files-success]}})))

(re-frame/reg-event-db
  ::show-code
  (fn [db _]
    (assoc-in db [:project :show] :code)))

(re-frame/reg-event-db
  ::list-files-success
  (fn [db [_ files]]
    (assoc-in db [:project :files] files)))

(defn update-code [current-code filename]
  (if (= "(+ 1 2)" current-code)
    (str "(file-read \"" filename "\")")
    (str current-code "\n\n(file-read \"" filename "\")")))

(re-frame/reg-event-db
  ::add-file-read
  (fn [db [_ filename]]
    (-> db
        (assoc-in [:project :show] :code)
        (assoc-in [:project :code] (update-code (get-in db [:project :code]) filename)))))

