(ns lloydshark.wrangler.server
  (:require [bidi.ring :as bidi-ring]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [clojure.pprint :as pprint]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.resource :as resource]
            [ring.util.response]
            [ring.middleware.format-response :refer [wrap-transit-json-response]]
            [ring.middleware.format-params :refer [wrap-transit-json-params]]
            [lloydshark.wrangler.log :as log]
            [lloydshark.wrangler.wrangle :as wrangle]
            [lloydshark.wrangler.store :as store]
            [zprint.core :as zprint])
  (:import (org.eclipse.jetty.server Server)
           (java.time LocalDateTime)))

(defn pretty-print [thing]
  (zprint/zprint-str thing {:map {:force-nl? true
                                  :justify?  true}}))

(defn pretty-print-if-not-string [thing]
  (if (string? thing)
    thing
    (pretty-print thing)))

(defn read-and-eval [{:keys [id input code]}]
  (try
    (binding [*ns*                 (find-ns 'lloydshark.wrangler.wrangle)
              wrangle/*input*      input
              wrangle/*project-id* id
              log/*logs*           (atom [])]
      (let [_               (log/info (str (LocalDateTime/now) "\n\n"))
            start-time      (System/currentTimeMillis)
            _               (Thread/sleep 50)
            pretty-result   (-> code
                                (read-string)
                                (eval)
                                (pretty-print-if-not-string)
                                )
            end-time        (System/currentTimeMillis)
            processing-time (- end-time start-time)
            _               (log/info (str "\nProcessing Time: " processing-time "ms"))]
        {:result pretty-result
         :logs   @log/*logs*
         :time   processing-time}))
    (catch Exception e
      (.printStackTrace e)
      {:error (if (.getCause e)
                (format "%s\n%s" (.getMessage e) (.getMessage (.getCause e)))
                (.getMessage e))})))

(defn evaluate [request]
  (let [result (read-and-eval (:params request))]
    {:status 200
     :body   result}))

(defn example [_request]
  {:status 200
   :body   (-> {:this "that"
                :the  {:other "fred"}}
               (json/generate-string))})

(defn post-example [_request]
  {:status 200
   :body   (-> {:this "that"
                :the  {:other "fred"}}
               (json/generate-string))})

(defn projects [_request]
  {:status 200
   :body   (store/projects)})

(defn project [request]
  {:status 200
   :body   (store/fetch-project (get-in request [:params :id]))})

(defn save-project [request]
  {:status 200
   :body   (store/save-project (:params request))})

(defn delete-project [request]
  (println "delete-project" (:params request))
  {:status 200
   :body   (store/delete-project (get-in request [:params :id]))})

(defn open-file-folder [request]
  {:status 200
   :body   (do (store/open-file-folder (get-in request [:params :id]))
               {:status "OK"})})

(defn list-files [request]
  {:status 200
   :body   (store/list-files (get-in request [:params :id]))})

(def handler
  (-> (bidi-ring/make-handler ["/"
                               {:post   {"evaluate" evaluate
                                         "example"  post-example
                                         "projects" save-project}
                                :delete {["projects/" :id] delete-project}
                                :get    {"example"                        example
                                         "projects"                       projects
                                         ["projects/" :id]                project
                                         ["projects/" :id "/file-folder"] open-file-folder
                                         ["projects/" :id "/files"]       list-files}}])
      (wrap-transit-json-response)
      (wrap-transit-json-params)
      (resource/wrap-resource "public")))

(defn start-server []
  (jetty/run-jetty (fn [request] (handler request))
                   {:port  8081
                    :join? false}))

(defn stop-server [^Server server]
  (.stop server))



(comment

  (.open (java.awt.Desktop/getDesktop)
         (clojure.java.io/file (System/getProperty "user.home")))

  my-server

  (def my-server (start-server))

  (pprint/pprint "2323")

  (stop-server my-server)

  (wrangle/http-get "http://localhost:8081/example")

  (wrangle/json-get "http://localhost:8081/example")

  (read-and-eval "(+ 1 2 banana)")

  (client/post "http://localhost:8081/evaluate"
               {:body              "(+ 1 2)"
                :throw-exceptions? false})

  (wrangle/json-post "http://localhost:8081/evaluate"
                     {:body              "(json-get \"http://localhost:8081/example\")"
                      :throw-exceptions? false})


  (zprint/zprint {:body {:the {:other "fred"}, :this "that" :x "dfadfg" :a 1 :b 2 :c 3}, :status 200})
  (zprint/zprint {:the {:other "fred"}, :this "that" :x "dfadfg" :a 1 :b 2 :c 3}
                 {:map {:force-nl? true
                        :justify?  true}})

  )