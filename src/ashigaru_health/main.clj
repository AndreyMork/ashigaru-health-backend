(ns ashigaru-health.main
  (:gen-class)
  (:require [ashigaru-health.handler :refer [get-app]]
            [ashigaru-health.config :refer [load-and-validate-config]]
            [orchestra.spec.test]
            [ring.adapter.jetty :refer [run-jetty]]))

(def patients-db
  (atom
   {1 {:id 1
       :first-name "John"
       :last-name "Doe"
       :patronim nil
       :birthdate "1990"
       :gender "male"
       :address "Street"
       :oms "0000000000000000"}
    2 {:id 2
       :first-name "Jane"
       :last-name "Doe"
       :patronim nil
       :birthdate "2000"
       :gender "female"
       :address "Street"
       :oms "0000000000000000"}}))

(defn -main
  [& _args]
  (let [{app-config :app
         server-config :server
         :as config} (load-and-validate-config)
        app (get-app {:db patients-db
                      :logging? true})]
    (when (= "development" (:env app-config))
      (orchestra.spec.test/instrument))
    (run-jetty app server-config)))
