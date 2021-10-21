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

(defn edit-input [e]
  (re-frame/dispatch [::events/edit-input (-> e .-target .-value)]))

;(defn home-panel []
;  )

(defn on-paste [event]
  (println "on-paste")
  (println (.-clipboardData event))
  (println (.-files (.-clipboardData event)))
  (println (.-items (.-clipboardData event))))

(defn library []
  [:svg.h-5.w-5 {:xmlns "http://www.w3.org/2000/svg" :viewBox "0 0 20 20" :fill "currentColor"}
   [:path {:fill-rule "evenodd" :d "M10.496 2.132a1 1 0 00-.992 0l-7 4A1 1 0 003 8v7a1 1 0 100 2h14a1 1 0 100-2V8a1 1 0 00.496-1.868l-7-4zM6 9a1 1 0 00-1 1v3a1 1 0 102 0v-3a1 1 0 00-1-1zm3 1a1 1 0 012 0v3a1 1 0 11-2 0v-3zm5-1a1 1 0 00-1 1v3a1 1 0 102 0v-3a1 1 0 00-1-1z" :clip-rule "evenodd"}]])

(defn folder []
  [:svg.h-5.w-5 {:xmlns "http://www.w3.org/2000/svg" :viewBox "0 0 20 20" :fill "currentColor"}
   [:path {:fill-rule "evenodd" :d "M2 6a2 2 0 012-2h4l2 2h4a2 2 0 012 2v1H8a3 3 0 00-3 3v1.5a1.5 1.5 0 01-3 0V6z" :clip-rule "evenodd"}]
   [:path {:d "M6 12a2 2 0 012-2h8a2 2 0 012 2v2a2 2 0 01-2 2H2h2a2 2 0 002-2v-2z"}]])

(defn home []
  [:svg.h-5.w-5 {:xmlns "http://www.w3.org/2000/svg" :viewBox "0 0 20 20" :fill "currentColor"}
   [:path {:d "M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z"}]])

(defn files-panel []
  (let [files (re-frame/subscribe [::subs/files])]
    [:div {:class "flex-grow"}
     (map (fn [filename]
            [:div {:class "hover:bg-gray-700"
                   :on-click #(re-frame/dispatch [::events/add-file-read filename])}
             filename])
          @files)]))

(defn code-panel []
  (let [code (re-frame/subscribe [::subs/code])]
    [:div {:class "flex flex-col flex-grow"}
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
       "Evaluate (Ctrl-Enter)"]]]))

(defn input-panel []
  (let [input (re-frame/subscribe [::subs/input])]
    [:div {:class "flex flex-col flex-grow"}
     [:textarea {:class     "flex-grow v-full w-full font-mono resize-none border-none outline-none bg-transparent"
                 :type      :text
                 :value     @input
                 :on-change edit-input
                 :on-blur   #(re-frame/dispatch [::events/save-project])}]]))

(defn result-panel []
  (let [evaluating (re-frame/subscribe [::subs/evaluating])
        result-tab (re-frame/subscribe [::subs/result-tab])
        input      (re-frame/subscribe [::subs/input])
        result     (re-frame/subscribe [::subs/result])
        logs       (re-frame/subscribe [::subs/logs])
        error      (re-frame/subscribe [::subs/error])]
    [:div {:class "flex flex-grow flex-col"}
     [:div {:class "flex justify-end"}
      [:button {:class (cond-> "border border-solid rounded hover:bg-blue-700 p-2 m-2"
                               (= :input @result-tab) (str " text-gray-900 bg-blue-100"))
                :on-click #(re-frame/dispatch [::events/set-result-tab :input])}
       "Input"]
      [:button {:class (cond-> "border border-solid rounded hover:bg-blue-700 p-2 m-2"
                               (= :output @result-tab) (str " text-gray-900 bg-blue-100"))
                :on-click #(re-frame/dispatch [::events/set-result-tab :output])}
       "Output"]
      [:button {:class (cond-> "border border-solid rounded hover:bg-blue-700 p-2 m-2"
                               (= :logs @result-tab) (str " text-gray-900 bg-blue-100"))
                :on-click #(re-frame/dispatch [::events/set-result-tab :logs])}
       "Logs"]
      [:button {:class (cond-> "border border-solid rounded hover:bg-blue-700 p-2 m-2"
                               (= :error @result-tab) (str " text-gray-900 bg-blue-100"))
                :on-click #(re-frame/dispatch [::events/set-result-tab :error])}
       "Error"]]
     (if @evaluating
       [:svg.h-10.w-10 {:class "animate-spin" :xmlns "http://www.w3.org/2000/svg" :viewBox "0 0 20 20" :fill "currentColor"}
        [:path {:fill-rule "evenodd" :d "M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z" :clip-rule "evenodd"}]]
       (case @result-tab
         :input [input-panel]
         :output [:pre @result]
         :logs [:div (for [log-entry @logs]
                       [:pre log-entry])]
         :error [:pre @error]
         @input))]))

(defn project-panel []
  (let [show       (re-frame/subscribe [::subs/code-show])]
    [:div {:class "flex-grow grid grid-cols-2"}
     [:div {:class "flex flex-col border border-solid rounded p-4 mr-4"}
      [:div {:class "flex justify-end my-2"}
       [:div {:class    "border border-solid rounded p-2"
              :on-click #(re-frame/dispatch [::events/open-files-folder])}
        [folder]]
       [:div {:class    "border border-solid rounded p-2"
              :on-click #(re-frame/dispatch [::events/show-code])}
        "Code"]
       [:div {:class    "border border-solid rounded p-2"
              :on-click #(re-frame/dispatch [::events/list-files])}
        "Files"]]
      (if (= :files @show)
        [files-panel]
        [code-panel])]
     [:div {:class "flex flex-grow flex-col border border-solid rounded p-4 ml-4"}
      [result-panel]
      ]]))

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
        project-id   (re-frame/subscribe [::subs/id])
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
                    :on-click #(re-frame/dispatch [::events/delete-project @project-id])}
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
        :project (project-panel))
      ]]))



