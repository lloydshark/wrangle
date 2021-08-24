(ns lloydshark.wrangler.wrangle
  (:require [cheshire.core :as json]
            [clojure.pprint :as pprint]
            [clj-http.client :as http]))

(defn pretty-print [thing]
  (with-out-str (pprint/pprint thing)))

(defn parse-json [json-string]
  (json/parse-string json-string true))

(defn write-json [thing]
  (json/write thing))

(defn http-get
  ([url] (http-get url nil))
  ([url options] (-> (http/get url options)
                     (select-keys [:status :body]))))

(defn http-post
  ([url] (http-post url nil))
  ([url options] (-> (http/post url options)
                     (select-keys [:status :body]))))

(defn json-get
  ([url] (json-get url nil))
  ([url options] (let [response (http-get url options)]
                   (-> (:body response)
                       (json/parse-string)))))

(defn json-post
  ([url] (json-post url nil))
  ([url options] (let [response (http-post url options)]
                   (-> (:body response)
                       (json/parse-string true)))))
