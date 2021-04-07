(ns ashigaru-health.app-test
  (:require
   [ashigaru-health.fixtures :as fixtures]
   [ashigaru-health.kaocha-hooks :refer [app]]
   [ashigaru-health.schemas :as schemas]
   [ashigaru-health.test-utils
    :as test-utils
    :refer [request
            no-id
            get-unknown-id
            build-patients-path]]
   [clojure.test :as t :refer [deftest]]
   [com.gfredericks.test.chuck.clojure-test :refer [checking]]
   [malli.generator :as mg]
   [testit.core
    :as testit
    :refer [facts => =not=> =in=>]]))
; 

(defn request-patients
  []
  (:body (request @app :get (build-patients-path))))

(deftest get-patients-not-allowed
  (doseq [method [:delete :put :patch]]
    (let [response (request @app method (build-patients-path))]
      (facts "`/patients` not allowed methods return 405"
             (:status response) => 405))))

(deftest get-patients
  (let [response (request @app :get (build-patients-path))
        patients (->> response
                      :body
                      (map no-id)
                      (into #{}))]
    (facts "`GET /patients` returns list of patients"
           (:status response) => 200
           patients => fixtures/patients)))

(deftest create-patient
  (checking
   "New patient body" 40
   [body (mg/generator schemas/new-patient-schema)]

   (let [path (build-patients-path)
         body {:last_name "\u0000"
               :birthdate "2000-20-20"
               :address ""
               :gender "abc"
               :oms "12345"}
         response (request @app :post path body)]
     (facts "`POST /patients` validates incoming data"
            (:status response) => 400))

   (let [path (build-patients-path)
         response (request @app :post path body)
         location (get-in response [:headers "Location"])
         location-response (request @app :get location)]
     (when-not (= (:status response) 409)
       (facts "`POST /patients`creates new patient"
              (:status response) => 201
              location =not=> nil
              (no-id (:body response)) => body
              (:status location-response) => 200
              (:body location-response) => (:body response))))

   (let [path (build-patients-path)
         response (request @app :post path body)]
     (facts "`POST /patients` handles oms uniqueness constraint"
            (:status response) => 409))))

(deftest get-patient
  (let [patients (request-patients)]
    (facts patients =not=> empty?)
    (doseq [patient patients]
      (let [path (build-patients-path (:id patient))
            response (request @app :get path)]
        (facts "`GET /patients/:id` returns patient info"
               (:status response) => 200
               (:body response) => patient))))

  (let [patients (request-patients)
        unknown-id (get-unknown-id patients)
        path (build-patients-path unknown-id)
        response (request @app :get path)]
    (facts patients =not=> empty?)
    (facts "`GET /patients/:id` returns not found if patient doesn't exist"
           (:status response) => 404)))

(deftest update-patient
  (let [patients (request-patients)
        first-patient (first patients)
        unknown-id (get-unknown-id patients)]

    (facts patients =not=> empty?)

    (let [id (:id first-patient)
          path (build-patients-path id)
          body1 {:first_name nil
                 :last_name 123
                 :address ""
                 :gender "abc"
                 :oms "12345"}
          body2 {:abc "abc"}
          response1 (request @app :patch path body1)
          response2 (request @app :patch path body2)]
      (facts "`PATCH /patients/:id` validates incoming data"
             (:status response1) => 400
             (:status response2) => 400))

    (doseq [patient patients]
      (checking
       "Update patient body" 10
       [body (mg/generator schemas/partial-patient-schema)]

       (let [path (build-patients-path (:id patient))
             response (request @app :patch path body)]
         (when-not (= (:status response) 409)
           (facts "`PATCH /patients/:id` updates patient"
                  (:status response) => 200
                  (:body response) =in=> body))))

      (let [oms_number "0000111122223333"
            path (build-patients-path (:id patient))
            response (request @app :patch path {:oms_number oms_number})]
        (when-not (= patient first-patient)
          (facts "`PATCH /patients/:id` handles oms uniqueness constraint"
                 (:status response) => 409))))

    (let [path (build-patients-path unknown-id)
          response (request @app :patch path {:first_name "abc"})]
      (facts "`PATCH /patients/:id` handles not found"
             (:status response) => 404))))

(deftest delete-patient
  (let [patients (request-patients)]
    (facts patients =not=> empty?)
    (doseq [patient patients]
      (let [id (:id patient)
            path (build-patients-path id)
            pre-delete-response (request @app :get path)
            first-delete-response (request @app :delete path)
            post-delete-response (request @app :get path)
            second-delete-response (request @app :delete path)]
        (facts "`DELETE /patient/:id` deletes patients"
               (:status pre-delete-response) => 200
               (:status first-delete-response) => 204
               (:status post-delete-response) => 404
               (:status second-delete-response) => 404)))
    (facts "No patients left"
           (request-patients) => empty?)))

