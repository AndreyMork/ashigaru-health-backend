(ns ashigaru-health.resources
  (:require
   [ashigaru-health.db.queries :as queries]
   [ashigaru-health.schemas :as schemas]
   [ashigaru-health.utils :as utils]
   [reitit.coercion.malli]
   [ring.util.http-response :as res]))

(def default-columns
  ["id"
   "first_name"
   "last_name"
   "patronim"
   "birthdate"
   "gender"
   "address"
   "oms_number"])

(def id-in-path-schema
  [:map [:id nat-int?]])

(def default-body-schema
  [:map [:message string?]])

(def patient-not-found-message
  {:message "Paitent not found"})

(defn get-patients
  [connection-pool]
  {:responses {200 {:body schemas/patients-schema}}
   :handler
   (fn [_]
     (let [patients (queries/get-patients
                     connection-pool
                     {:columns default-columns})]
       (res/ok patients)))})

(defn get-patient
  [connection-pool]
  {:responses {200 {:body schemas/patient-schema}
               404 {:body default-body-schema}}
   :parameters {:path id-in-path-schema}
   :handler
   (fn [{:keys [path-params]}]
     (let [id (Integer/parseInt (:id path-params))
           patient (queries/get-patient
                    connection-pool
                    {:id id :columns default-columns})]
       (if patient
         (res/ok patient)
         (res/not-found patient-not-found-message))))})

(defn new-patient
  [connection-pool]
  {:responses {200 {:body schemas/patient-schema}
               409 {:body default-body-schema}}
   :parameters {:body schemas/new-patient-schema}
   :handler
   (fn [{:keys [body-params] :as request}]
     (let [sql-params (-> body-params
                          utils/sqlize-patient
                          (assoc :returning default-columns))
           new-patient (queries/new-patient! connection-pool sql-params)
           location-url (utils/build-location-url request (:id new-patient))]
       (res/created location-url new-patient)))})

(defn update-patient
  [connection-pool]
  {:responses {200 {:body schemas/patient-schema}
               404 {:body default-body-schema}
               409 {:body default-body-schema}}
   :parameters {:path id-in-path-schema
                :body schemas/partial-patient-schema}
   :handler
   (fn [{:keys [body-params path-params]}]
     (let [id (Integer/parseInt (:id path-params))
           prepared-params (utils/sqlize-patient body-params)
           sql-params {:id id
                       :values prepared-params
                       :returning default-columns}
           updated-patient (queries/update-patient!
                            connection-pool
                            sql-params)]
       (if updated-patient
         (res/ok updated-patient)
         (res/not-found patient-not-found-message))))})

(defn delete-patient
  [connection-pool]
  {:responses {204 {}
               404 {:body default-body-schema}}
   :parameters {:path id-in-path-schema}
   :handler
   (fn [{:keys [path-params]}]
     (let [id (Integer/parseInt (:id path-params))
           deleted-id (queries/delete-patient!
                       connection-pool
                       {:id id})]
       (if deleted-id
         (res/no-content)
         (res/not-found patient-not-found-message))))})

