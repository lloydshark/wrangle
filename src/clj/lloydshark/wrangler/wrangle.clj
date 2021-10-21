(ns lloydshark.wrangler.wrangle
  (:require [cheshire.core :as json]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [clj-http.client :as http]
            [lloydshark.wrangler.log :as log]
            [lloydshark.wrangler.store :as store]))

(def ^:dynamic *input*)
(def ^:dynamic *project-id*)

(defn pretty-print [thing]
  (with-out-str (pprint/pprint thing)))

(defn parse-json [json-string]
  (json/parse-string json-string true))

(defn write-json [thing]
  (json/generate-string thing))

(defn file-read [filename]
  (let [filepath (str (store/project-files-dir *project-id*) "/" filename)]
    (println "Slurping... " filepath)
    (slurp filepath)))

(defn http-get
  ([url] (http-get url nil))
  ([url options]
   (let [_        (log/info (str ">>>>> REQUEST:\n\nGET " url))
         response (http/get url options)
         _        (log/info (str "\n>>>>> RESPONSE:\n\nSTATUS: " (:status response)))
         _        (log/info (str "\n" (:body response)))]
     (select-keys response [:status :body]))))

(defn http-post
  ([url] (http-post url nil))
  ([url options] (-> (http/post url options)
                     (select-keys [:status :body]))))

(defn json-get
  ([url] (json-get url nil))
  ([url options]
   (let [json-options   (assoc-in options [:headers "Content-Type"] "application/json")
         -              (log/info (format ">>>>> REQUEST >>>>>\n\nGET %s\n\n" url))
         _              (doseq [[header-name header-value] (:headers json-options)]
                          (log/info (format "%s:%s" header-name header-value)))
         response       (http/get url json-options)
         _              (log/info (format "\n<<<<< RESPONSE <<<<<\n\nSTATUS %s\n\n" (:status response)))
         _              (doseq [[header-name header-value] (:headers response)]
                          (log/info (format "%s:%s" header-name header-value)))
         formatted-json (-> (:body response)
                            (json/parse-string)
                            (json/generate-string {:pretty true}))
         _              (log/info (str "\n" formatted-json))
         ]
     formatted-json)))

(defn json-post
  ([url json-body] (json-post url json-body nil))
  ([url json-body options]
   (let [formatted-json-input (-> json-body
                                  (json/parse-string)
                                  (json/generate-string {:pretty true}))
         json-options         (-> options
                                  (assoc-in [:headers "Content-Type"] "application/json")
                                  (assoc :body formatted-json-input))
         -                    (log/info (format ">>>>> REQUEST >>>>>\n\nGET %s\n\n" url))
         _                    (doseq [[header-name header-value] (:headers json-options)]
                                (log/info (format "%s:%s" header-name header-value)))
         _                    (log/info (str "\n" formatted-json-input))
         response             (http/post url json-options)
         _                    (log/info (format "\n<<<<< RESPONSE <<<<<\n\nSTATUS %s\n\n" (:status response)))
         _                    (doseq [[header-name header-value] (:headers response)]
                                (log/info (format "%s:%s" header-name header-value)))
         formatted-json       (-> (:body response)
                                  (json/parse-string)
                                  (json/generate-string {:pretty true}))
         _                    (log/info (str "\n" formatted-json))
         ]
     formatted-json)))

(comment

  (file-slurp "another-new-project" "wrangle.data")

  )