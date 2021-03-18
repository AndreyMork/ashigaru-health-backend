(ns ashigaru-health.main
  (:gen-class)
  (:require [ashigaru-health.handler :refer [get-app]]
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
  (let [port (Integer/parseInt (System/getenv "PORT"))
        app (get-app {:db patients-db})]
    (run-jetty app {:port port})))
