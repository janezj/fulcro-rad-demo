(ns devmain
  (:require [development :refer [start]]
            [mount.core :as mount]
            [nrepl.server :refer [start-server stop-server]])
  (:gen-class))

(mount/defstate ^{:on-reload :noop} nrepl
  :start (start-server :port 9000)
  :stop (stop-server nrepl))

(defn -main [& args]
  (start))
