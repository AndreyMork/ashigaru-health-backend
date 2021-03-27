(ns ashigaru-health.main
  (:gen-class)
  (:require [ashigaru-health.app :refer [get-app]]
            [ashigaru-health.config :as config]
            [ashigaru-health.db.core :as db]
            [orchestra.spec.test]
            [ring.adapter.jetty :refer [run-jetty]]))

(defn -main
  [& _args]
  (let [{app-config :app
         server-config :server
         db-config :db} (config/load-and-validate-config)
        db-datasource (db/get-connection-pool db-config)
        app (get-app {:logging? (:logging app-config)
                      :connection-pool db-datasource})]
    (when (= "development" (:env app-config))
      (orchestra.spec.test/instrument))
    (db/setup-all!)
    (db/test-connection db-datasource)
    (run-jetty app server-config)))
