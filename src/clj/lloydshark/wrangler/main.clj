(ns lloydshark.wrangler.main
  (:require [lloydshark.wrangler.server :as server])
  (:import (java.util.concurrent CountDownLatch))
  (:gen-class))

(defn run [& args]
  (println args)
  (let [latch  (CountDownLatch. 1)
        _      (println "Starting Server...")
        server (server/start-server)]
    (.addShutdownHook
      (Runtime/getRuntime)
      (Thread. ^Runnable
               (fn []
                 (println "Shutting Down...")
                 (server/stop-server server)
                 (.countDown latch))))
    (.await latch)
    (System/exit 0)))