(ns featured-gml.filesystem
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [clj-time.core :as time]
            clj-time.coerce)
  (:import (java.util UUID)))

(def resultstore
  (let [separator (System/getProperty "file.separator")
        path (io/file (or (env :featured-gml.jsonstore)
                          (str (System/getProperty "java.io.tmpdir") separator "featured-gml" separator)))]
    (when-not (.exists path) (.mkdirs path))
    path))

(defn safe-delete [file-path]
  (log/debug "Going to delete" file-path)
  (if (.exists (io/file file-path))
    (try
      (clojure.java.io/delete-file file-path)
      (catch Exception e (str "exception: " (.getMessage e))))
    false))

(defn delete-files [files]
  (doseq [file files]
    (safe-delete file)))

(defn cleanup-old-files [threshold-seconds]
  (let [files (.listFiles resultstore)
        threshold (clj-time.coerce/to-long (time/minus (time/now) (time/seconds threshold-seconds)))
        old-files (filter #(< (.lastModified %) threshold) files)]
    (delete-files old-files)
    old-files))

(defn uuid []
  (str (UUID/randomUUID)))

(defn create-target-file [name]
  (io/file resultstore (str (uuid) "-" name ".json")))

(defn get-file [name]
  (let [file (io/file resultstore name)]
    (when (.exists file) file)))