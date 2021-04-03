(ns ashigaru-health.system
  (:require
   [ashigaru-health.config :as config]
   [ashigaru-health.db.coerce :as db.coerce]
   [ashigaru-health.db.core :as db]
   [ashigaru-health.handler :as handler]
   [ashigaru-health.resources :as resources]
   [ashigaru-health.router :as router]
   [integrant.core :as ig]
   [ring.adapter.jetty :as jetty]))

(defmethod ig/init-key :config/load [_ overrides]
  (config/load-config overrides))

(defmethod ig/init-key :runtime/setup [_ _]
  (db.coerce/coerce-all!))

(defmethod ig/init-key :db/connection-pool [_ {:keys [config]}]
  (let [db-config (:db config)
        connection (db/get-connection-pool db-config)]
    (db/test-connection connection)))

(defmethod ig/init-key :resources/get-patients
  [_ {:keys [connection-pool]}]
  (resources/get-patients connection-pool))

(defmethod ig/init-key :resources/create-patient
  [_ {:keys [connection-pool]}]
  (resources/create-patient connection-pool))

(defmethod ig/init-key :resources/get-patient
  [_ {:keys [connection-pool]}]
  (resources/get-patient connection-pool))

(defmethod ig/init-key :resources/update-patient
  [_ {:keys [connection-pool]}]
  (resources/update-patient connection-pool))

(defmethod ig/init-key :resources/delete-patient
  [_ {:keys [connection-pool]}]
  (resources/delete-patient connection-pool))

(defmethod ig/init-key :app/router [_ {:keys [routes config]}]
  (router/get-router routes config))

(defmethod ig/init-key :app/handler [_ {:keys [router]}]
  (handler/get-handler router))

(defmethod ig/init-key :app/server [_ {:keys [config handler]}]
  (let [server-config (:server config)]
    (jetty/run-jetty handler server-config)))

(defmethod ig/halt-key! :app/server [_ server]
  (.stop server))
