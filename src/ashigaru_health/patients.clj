(ns ashigaru-health.patients
  (:require [ashigaru-health.specs.patient :as patient-spec]
            [orchestra.core :refer [defn-spec]]))

(defn next-id
  [db]
  (->> @db
       keys
       (apply max 0)
       inc))

(defn get-patients
  [db]
  (vals @db))

(defn-spec get-patient-by-id ::patient-spec/maybe-patient
  [db some? id integer?]
  (get @db id))

(defn-spec new-patient ::patient-spec/patient
  [db some? params ::patient-spec/patient-no-id]
  (let [{:keys [first-name
                last-name
                patronim
                birthdate
                address
                gender
                oms]} params
        id (next-id db)
        new-patient {:id id
                     :first-name first-name
                     :last-name last-name
                     :patronim patronim
                     :birthdate birthdate
                     :address address
                     :gender gender
                     :oms oms}]
    (reset! db (assoc @db id new-patient))
    new-patient))

(defn-spec update-patient ::patient-spec/maybe-patient
  [db some? id int? params ::patient-spec/partial-patient]
  (when-let [patient (get @db id)]
    (let [update-values (select-keys params
                                     [:first-name
                                      :last-name
                                      :patronim
                                      :birthdate
                                      :gender
                                      :address
                                      :oms])
          updated-patient (merge patient update-values)]
      (reset! db (assoc @db id updated-patient))
      updated-patient)))

(defn-spec delete-patient-by-id nil?
  [db some? id int?]
  (when (contains? @db id) (reset! db (dissoc @db id)))
  nil)
