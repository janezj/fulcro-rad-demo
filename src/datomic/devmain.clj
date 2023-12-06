(ns devmain
  (:require [development :refer [start]]
            [mount.core :as mount]
            [nrepl.server :refer [start-server stop-server]])
  (:gen-class))

(defn- start-nprepl []
  (let [port 9000]
    (println "Starting nREPL server on port " port)
    (try
      (start-server :port port)
      (catch Exception e
        (println "REPL: caught exception:" (.getMessage e))
        nil))))

(mount/defstate ^{:on-reload :noop} nrepl
  :start (start-nprepl)
  :stop (when nrepl 
          (stop-server nrepl)))

(defn -main [& args]
  (start))
