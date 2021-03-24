(ns ashigaru-health.patients.handler
  (:require [ashigaru-health.patients.spec :as patient-spec]
            [ashigaru-health.db.queries :as queries]
            [ashigaru-health.utils :as utils]
            [orchestra.core :refer [defn-spec]]))

(defn ->JSONify
  [patient]
  (-> patient
      (utils/update-if-exists :birthdate str)))

(def default-columns
  ["id"
   "first_name"
   "last_name"
   "patronim"
   "birthdate"
   "gender"
   "address"
   "oms_number"])

(def default-params-keys
  (->> default-columns (filter #(not= "id" %)) (map keyword)))

(defn-spec get-patients ::patient-spec/patients
  [connection some?]
  (let [rows (queries/get-patients connection {:columns default-columns})]
    (map ->JSONify rows)))

(defn-spec get-patient-by-id ::patient-spec/maybe-patient
  [connection some? id integer?]
  (when-let [patient (queries/get-patient-by-id connection {:id id
                                                            :columns default-columns})]
    (->JSONify patient)))

(defn-spec new-patient! ::patient-spec/patient
  [connection some? params ::patient-spec/patient-no-id]
  (let [new-patient (select-keys params default-params-keys)
        res (->> new-patient
                 utils/sqlize-patient
                 (#(assoc % :returning default-columns))
                 (queries/new-patient! connection)
                 ->JSONify)]
    res))

(defn-spec update-patient! ::patient-spec/maybe-patient
  [connection some? id int? params ::patient-spec/partial-patient]
  (let [values (select-keys params default-params-keys)]
    (if-not (empty? values)
      (when-let [patient (queries/update-patient-by-id! connection
                                                        {:id id
                                                         :returning default-columns
                                                         :values (utils/sqlize-patient values)})]
        (->JSONify patient))
      (get-patient-by-id connection id))))

(defn-spec delete-patient-by-id! nil?
  [connection some? id int?]
  (queries/delete-patient-by-id! connection {:id id})
  nil)
