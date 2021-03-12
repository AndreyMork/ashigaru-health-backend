(ns ashigaru-health.main
  (:gen-class)
  (:require [ashigaru-health.handler :refer [app]]
            [ring.adapter.jetty :refer [run-jetty]]))

(defn -main
  [& _args]
  (let [port (Integer/parseInt (System/getenv "PORT"))]
    (run-jetty app {:port port})))
