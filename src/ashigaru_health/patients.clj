(ns ashigaru-health.patients)

(defn next-id
  [db]
  (->> @db
       keys
       (apply max 0)
       inc))

(defn get-patients
  [db]
  (vals @db))

(defn get-patient-by-id
  [db id]
  (get @db id))

(defn new-patient
  [db {:keys [first-name
              last-name
              patronim
              birthdate
              address
              gender
              oms]}]
  (let [id (next-id db)
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

(defn update-patient
  [db id params]
  (when-let [patient (get @db id)]
    (let [update-values (select-keys
                         params
                         [:first-name :last-name :patronim :birthdate :gender :address :oms])
          updated-patient (merge patient update-values)]
      (reset! db (assoc @db id updated-patient))
      updated-patient)))

(defn delete-patient-by-id
  [db id]
  (when (contains? @db id) (reset! db (dissoc @db id))))
