(ns lloydshark.wrangler.store
  (:require [clojure.java.io :as io])
  (:import [java.io File]))

(defn projects-dir []
  (str (System/getProperty "user.home") "/.wrangle"))

(defn project-dir [project-id]
  (str (projects-dir) "/" project-id))

(defn wrangle-data-path [project-id]
  (str (project-dir project-id) "/wrangle.data"))

(defn wrangle-info-path [project-id]
  (str (project-dir project-id) "/wrangle.prj"))

(defn serialize-data [data]
  (pr-str data))

(defn save-wrangle-data [project-id wrangle-data]
  (spit (wrangle-data-path project-id)
        (serialize-data wrangle-data)))

(defn load-wrangle-data [project-id]
  (-> (wrangle-data-path project-id)
      (slurp)
      (read-string)))

(defn load-wrangle-info [project-id]
  (-> (wrangle-info-path project-id)
      (slurp)
      (read-string)))

(defn projects []
  (->> (file-seq (io/file (projects-dir)))
       (filter (fn [^File file] (and (.isFile file)
                                     (= (.getName file)
                                        "wrangle.prj"))))
       (map slurp)
       (map read-string)
       (reduce (fn [projects project]
                 (println project projects)
                 (assoc projects (:id project) project))
               {})))

(defn fetch-project [project-id]
  (merge
    (load-wrangle-info project-id)
    (load-wrangle-data project-id)))

(defn project-exists? [project-id]
  (.exists (io/file (wrangle-info-path project-id))))

(defn find-unique-project-id [base-id number]
  (let [project-id (str base-id "-" number)]
    (if (project-exists? project-id)
      (find-unique-project-id base-id (inc number))
      project-id)))

(defn generate-project-id-from-name [project-name]
  (let [project-id (-> project-name
                       (clojure.string/replace #"[^a-zA-Z\d\\s:]" "-")
                       (clojure.string/replace #"[-]+" "-")
                       (.toLowerCase))]
    (if (project-exists? project-id)
      (find-unique-project-id project-id 2)
      project-id)))

(defn create-project-directory [project-id]
  (.mkdir (io/file (project-dir project-id))))

(defn create-project-id [project]
  (let [project-id (generate-project-id-from-name (:name project))]
    (create-project-directory project-id)
    project-id))

(defn get-or-create-project-id [project]
  (or (:id project)
      (create-project-id project)))

(defn save-wrangle-info [project-id project-info]
  (spit (wrangle-info-path project-id)
        (serialize-data project-info)))

(defn save-wrangle-data [project-id project-data]
  (spit (wrangle-data-path project-id)
        (serialize-data project-data)))

(defn save-project [project]
  (let [project-id (get-or-create-project-id project)]
    (save-wrangle-info project-id {:id   project-id
                                   :name (:name project)})
    (save-wrangle-data project-id {:code (:code project)})
    (fetch-project project-id)))

(comment

  (projects)

  (->> (take 10 (file-seq (clojure.java.io/file (System/getProperty "user.home"))))
       (map (fn [^java.io.File file] (and (.isFile file)
                                          (.getName file)))))

  )