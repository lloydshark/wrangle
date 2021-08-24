(ns wrangler-ui.db)

(def default-db
  {:screen   :home
   :projects {}})

(comment

  {:screen   :home
   :projects {"mygreatdemo" {:name "My Great Demo"
                             :id   "mygreatdemo"}
              "myotherdemo" {:name "My Other Demo"
                             :id   "myotherdemo"}
              ; ...
              }
   :project  {:name "My Great Demo"
              :id   "mygreatdemo"
              :data {:code   "(+ 1 2"
                     :result "fred"}}})