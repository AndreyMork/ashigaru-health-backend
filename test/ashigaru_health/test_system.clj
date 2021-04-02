(ns ashigaru-health.test-system
  (:require
   [ashigaru-health.db.core :as db]
   [ashigaru-health.db.queries :as queries]
   [ashigaru-health.fixtures :as fixtures]
   [ashigaru-health.system]
   [ashigaru-health.test-utils :as test-utils]
   [clj-test-containers.core :as containers]
   [integrant.core :as ig]))

(defmethod ig/init-key :test/fixtures [_ _]
  {:patients fixtures/patients})

(defmethod ig/init-key :test/container [_ {:keys [config]}]
  (let [container (containers/create-from-docker-file (:test config))]
    (containers/start! container)))

(defmethod ig/halt-key! :test/container [_ container]
  (containers/stop! container))

(defmethod ig/suspend-key! :test/container [_ container]
  container)

(defmethod ig/resume-key :test/container [_ _ _ container]
  container)

(defmethod ig/init-key :db/connection-pool [_ {:keys [container config fixtures]}]
  (let [{:keys [exposed-ports
                db-name
                env-vars]} (:test config)
        host (:host container)
        port (get (:mapped-ports container) (first exposed-ports))
        username (get env-vars "POSTGRES_USER")
        password (get env-vars "POSTGRES_PASSWORD")
        test-db-name (test-utils/generate-db-name db-name)
        db-config-base {:username username
                        :password password
                        :host host
                        :port port}
        main-db-connection (-> db-config-base
                               (assoc :database db-name)
                               db/get-connection-pool)
        test-db-connection (-> db-config-base
                               (assoc :database test-db-name)
                               db/get-connection-pool)]
    (queries/create-test-db!
     main-db-connection
     {:db-name test-db-name
      :template-db-name db-name})
    (test-utils/populate-test-db!
     test-db-connection
     (:patients fixtures))
    test-db-connection))
