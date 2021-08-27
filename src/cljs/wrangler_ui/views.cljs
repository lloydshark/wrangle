(ns wrangler-ui.views
  (:require
    [re-frame.core :as re-frame]
    [wrangler-ui.events :as events]
    [wrangler-ui.subs :as subs]))


(defn maybe-evaluate [e]
  ;(println e)
  (when (and (.-ctrlKey e)
             (= 13 (.-keyCode e)))
    (re-frame/dispatch [::events/evaluate])))

(defn edit-code [e]
  (re-frame/dispatch [::events/edit-code (-> e .-target .-value)]))

;(defn home-panel []
;  )

(defn on-paste [event]
  (println "on-paste")
  (println (.-clipboardData event))
  (println (.-files (.-clipboardData event)))
  (println (.-items (.-clipboardData event))))

(defn code-panel []
  (let [code   (re-frame/subscribe [::subs/code])
        result (re-frame/subscribe [::subs/result])]
    [:div {:class "flex-grow grid grid-cols-2"}
     [:div {:class "flex flex-col border border-solid rounded p-4 mr-4"}
      [:textarea {:class     "flex-grow w-full font-mono resize-none border-none outline-none bg-transparent"
                  :type      :text
                  :value     @code
                  :on-keyUp  maybe-evaluate
                  :on-change edit-code
                  :on-paste  on-paste
                  :on-blur   #(re-frame/dispatch [::events/save-project])}]
      [:div
       [:button {:class    "bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
                 :on-click #(re-frame/dispatch [::events/evaluate])}
        "Evaluate (Ctrl-Enter)"]]]
     [:div {:class "border border-solid rounded p-4 ml-4"}
      [:pre @result]]]))

(defn home-panel []
  (let [projects (re-frame/subscribe [::subs/projects])]
    [:div {:class "h-full w-full font-mono"}
     (for [project @projects]
       [:div {:key      (:id project)
              :class    "hover:bg-gray-700"
              :on-click #(re-frame/dispatch [::events/load-project (:id project)])}
        (:name project)])
     ]))

(defn project-loading []
  [:div "Loading..."])

(defn main-panel []
  (let [screen       (re-frame/subscribe [::subs/screen])
        project-name (re-frame/subscribe [::subs/name])
        edit-name?   (re-frame/subscribe [::subs/edit-name?])]
    [:div {:class "flex flex-col h-full w-full p-4"}
     [:div {:class "flex-none"}
      [:div {:class "flex flex-row"}
       [:div {:class "flex-grow text-2xl"}
        (if (= :project @screen)
          (if @edit-name?
            [:input {:class      "w-full font-mono resize-none bg-transparent"
                     :type       :text
                     :value      @project-name
                     :on-change  #(re-frame/dispatch [::events/edit-name (-> % .-target .-value)])
                     :on-blur    #(re-frame/dispatch [::events/edit-name-end])
                     :auto-focus true}]
            [:div {:class    "hover:bg-gray-700"
                   :on-click #(re-frame/dispatch [::events/edit-name-start])}
             @project-name])
          "Wrangle")]
       (when (= :project @screen)
         [:div {:class "space-x-2"}
          [:button {:class    "bg-red-700 hover:bg-red-500 text-white font-bold py-2 px-4 rounded"
                    ;:on-click #(if (js/confirm "banana")
                    ;              (println "Yes")
                    ;              (println "No")
                    ;             ;re-frame/dispatch [::events/delete-project]
                    ;            )
                    }
           "Delete Project"]
          [:button {:class    "bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
                    :on-click #(re-frame/dispatch [::events/close-project])}
           "Close Project"]])
       (when (= :home @screen)
         [:button {:class    "bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
                   :on-click #(re-frame/dispatch [::events/new-project])}
          "New Project"])
       ]]
     [:div {:class "flex flex-grow py-4"}
      (case @screen
        :home (home-panel)
        :project-loading (project-loading)
        :project (code-panel))
      ]]))
      
