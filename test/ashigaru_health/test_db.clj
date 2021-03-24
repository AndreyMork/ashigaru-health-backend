(ns ashigaru-health.test-db
  (:require [ashigaru-health.db.queries :as queries]
            [ashigaru-health.db.core :as db]
            [ashigaru-health.utils :as utils]
            [clj-test-containers.core :as containers]))

(def exposed-port 5432)
(def image-name "ehr-db")
(def db-username "postgres")
(def db-password "postgres")
(def db-name "ehr")

(def image-env
  {"POSTGRES_PASSWORD" db-password
   "POSTGRES_USER" db-username})

; (defn start-test-container!
;   []
;   (-> (containers/create {:env-vars image-env
;                           :exposed-ports [exposed-port]
;                           :image-name image-name
;                           :wait-for {:wait-strategy :health}})
;       containers/start!))

(defn start-test-container!
  []
  (-> (containers/create-from-docker-file {:env-vars image-env
                                           :exposed-ports [exposed-port]
                                           :docker-file "test-db/Dockerfile"
                                           :wait-for {:wait-strategy :health}})
      containers/start!))

(defn create-test-db!
  [{:keys [connection db-name template-db-name]}]
  (queries/create-test-db! connection {:db-name db-name
                                       :template-db-name template-db-name})
  connection)

(defn populate-test-db!
  [connection patients]
  (->> patients
       (map utils/sqlize-patient)
       (map #(assoc % :returning ["id"]))
       (run! #(queries/new-patient! connection %)))
  connection)

(defn generate-db-name
  [base]
  (str base (rand-int 1000000)))

(defn get-test-db!
  [container patients]
  (let [host (:host container)
        port (get (:mapped-ports container) exposed-port)
        test-db-name (generate-db-name db-name)
        db-config-base {:username db-username
                        :password db-password
                        :host host
                        :port port}
        main-db-datasource (-> db-config-base
                               (assoc :database db-name)
                               db/get-connection-pool)
        test-db-datasource (-> db-config-base
                               (assoc :database test-db-name)
                               db/get-connection-pool)]
    (create-test-db! {:connection main-db-datasource
                      :db-name test-db-name
                      :template-db-name db-name})
    (populate-test-db! test-db-datasource patients)
    test-db-datasource))
