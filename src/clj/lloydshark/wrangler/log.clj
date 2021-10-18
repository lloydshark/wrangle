(ns lloydshark.wrangler.log)

(def ^:dynamic *logs*)

(defn info [message]
  (swap! *logs* conj message))