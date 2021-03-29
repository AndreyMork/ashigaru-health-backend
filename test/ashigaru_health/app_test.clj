(ns ashigaru-health.app_test
  (:require
   [ashigaru-health.fixtures :as fixtures]
   [ashigaru-health.schemas :as schemas]
   [ashigaru-health.system]
   [ashigaru-health.system-test]
   [ashigaru-health.test-utils
    :as test-utils
    :refer [simple-request
            with-json-body
            simple-json-request
            no-id
            get-unknown-id
            build-patients-path]]
   [integrant.core :as ig]
   [malli.generator :as mg]
   [midje.experimental :refer [for-all]]
   [midje.sweet
    :as midje
    :refer [=> =not=> fact]]
   [ring.mock.request :as mock]))

(def config (test-utils/load-test-system-config))

(def system (atom nil))
(def app (atom nil))

(ig/init config)

(defn get-patients
  []
  (:body (simple-json-request @app :get (build-patients-path))))

(midje/with-state-changes
  [(midje/before :contents (do (reset! system (ig/init config))
                               (reset! app (:app/handler @system))))
   (midje/before :facts (do (reset! system (ig/resume config @system))
                            (reset! app (:app/handler @system))))
   (midje/after :facts (ig/suspend! @system))
   (midje/after :contents (ig/halt! @system))]
  (fact "abc"
        true => true)

  (fact "`GET /patients` returns list of patients"
        (let [response (simple-json-request @app :get (build-patients-path))
              patients (->> response
                            :body
                            (map no-id)
                            (into #{}))]
          (:status response) => 200
          patients => fixtures/patients))

  (midje/facts
   "About `POST /patients`"
   (for-all
    [body (mg/generator schemas/new-patient-schema)]

    (fact "`POST /patients`creates new patient"
          (let [path (build-patients-path)
                response (-> (mock/request :post path)
                             (mock/json-body body)
                             (@app)
                             (with-json-body))
                location (get-in response [:headers "Location"])
                location-response (simple-json-request @app :get location)]
            (when-not (= (:status response) 409)
              (:status response) => 201
              location =not=> nil
              (no-id (:body response)) => body
              (:status location-response) => 200
              (:body location-response) => (:body response))))

    (fact "`POST /patients` handles oms uniqueness constraint"
          (let [path (build-patients-path)
                response (-> (mock/request :post path)
                             (mock/json-body body)
                             (@app)
                             (with-json-body))]
            (:status response) => 409)))

   (fact "`POST /patients` validates incoming data"
         (let [path (build-patients-path)
               body {:first_name nil
                     :last_name 123
                     :patronim true
                     :address ""
                     :gender "abc"
                     :oms "12345"}
               response (-> (mock/request :post path)
                            (mock/json-body body)
                            (@app))]
           (:status response) => 400)))

  (midje/tabular
   (fact "`/patients` not allowed methods return 405"
         (let [response (simple-request @app ?method (build-patients-path))]
           (:status response) => 405))
   ?method
   :delete
   :put
   :patch)

  (fact "`GET /patients/:id` returns patient info"
        (let [patients (get-patients)]
          (doseq [patient patients]
            (let [path (build-patients-path (:id patient))
                  response (simple-json-request @app :get path)]
              (:status response) => 200
              (:body response) => patient))))

  (fact "`GET /patients/:id` returns not found if patient doesn't exist"
        (let [patients (get-patients)
              unknown-id (get-unknown-id patients)
              path (build-patients-path unknown-id)
              response (simple-request @app :get path)]
          (:status response) => 404))

  (midje/facts
   "About PATCH"

   (let [patients (get-patients)
         first-patient (first patients)]
     (doseq [patient patients]
       (for-all
        [body (mg/generator schemas/partial-patient-schema)]
        (fact "`PATCH /patients/:id` updates patient"
              (let [path (build-patients-path (:id patient))
                    response (-> (mock/request :patch path)
                                 (mock/json-body body)
                                 (@app)
                                 (with-json-body))]
                (when-not (= (:status response) 409)
                  (:status response) => 200
                  (:body response) => (midje/contains body)))))

       (fact "`PATCH /patients/:id` handles oms uniqueness constraint"
             (when-not (= patient first-patient)
               (let [oms_number (:oms_number first-patient)
                     path (build-patients-path (:id patient))
                     response (-> (mock/request :patch path)
                                  (mock/json-body {:oms_number oms_number})
                                  (@app)
                                  (with-json-body))]
                 (:status response) => 409))))

     (fact "`PATCH /patients/:id` validates incoming data"
           (let [id (:id (first patients))
                 path (build-patients-path id)
                 body {:first_name nil
                       :last_name 123
                       :patronim true
                       :address ""
                       :gender "abc"
                       :oms "12345"}
                 response (-> (mock/request :post path)
                              (mock/json-body body)
                              (@app))]
             (:status response) => 405))

     (fact "`PATCH /patients/:id` handles not found"
           (let [unknown-id (get-unknown-id patients)
                 path (build-patients-path unknown-id)
                 response (-> (mock/request :patch path)
                              (mock/json-body {:first_name "abc"})
                              (@app)
                              (with-json-body))]
             (:status response) => 404))))

  (let [patients (get-patients)]
    (fact "`DELETE /patient/:id` deletes patients"
          (doseq [patient patients]
            (let [id (:id patient)
                  path (build-patients-path id)
                  pre-delete-response (simple-request @app :get path)
                  first-delete-response (simple-request @app :delete path)
                  post-delete-response (simple-request @app :get path)
                  second-delete-response (simple-request @app :delete path)]
              (:status pre-delete-response) => 200
              (:status first-delete-response) => 204
              (:status post-delete-response) => 404
              (:status second-delete-response) => 404))))

  (midje/tabular
   (fact "`/patients/:id` not allowed methods return 405"
         (let [response (simple-request @app ?method (build-patients-path 0))]
           (:status response) => 405))
   ?method
   :put
   :post))
