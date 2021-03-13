(ns ashigaru-health.patients)

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

(defn next-id
  []
  (->> @patients-db
       keys
       (apply max 0)
       inc))

(defn get-patients
  []
  (vals @patients-db))

(defn get-patient-by-id
  [id]
  (get @patients-db id))

(defn new-patient
  [{:keys [first-name
           last-name
           patronim
           birthdate
           address
           gender
           oms]}]
  (let [id (next-id)
        new-patient {:id id
                     :first-name first-name
                     :last-name last-name
                     :patronim patronim
                     :birthdate birthdate
                     :address address
                     :gender gender
                     :oms oms}]
    (reset! patients-db (assoc @patients-db id new-patient))
    new-patient))

(defn update-patient
  [id params]
  (when-let [patient (get @patients-db id)]
    (let [update-values (select-keys
                         params
                         [:first-name :last-name :patronim :birthdate :gender :address :oms])
          updated-patient (merge patient update-values)]
      (reset! patients-db (assoc @patients-db id updated-patient))
      updated-patient)))

(defn delete-patient-by-id
  [id]
  (when (contains? @patients-db id) (reset! patients-db (dissoc @patients-db id))))
