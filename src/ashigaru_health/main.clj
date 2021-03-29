(ns ashigaru-health.main
  (:gen-class)
  (:require
   [ashigaru-health.system]
   [ashigaru-health.utils :as utils]
   [integrant.core :as ig]))

(defn -main
  [& _args]
  (let [system-config (utils/load-system-config)
        system (ig/init system-config)]
    system))
