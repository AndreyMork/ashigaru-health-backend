(ns ashigaru-health.app_test
  (:require
   [ashigaru-health.app :refer [get-app]]
   [ashigaru-health.db.coerce :as db-coerce]
   [ashigaru-health.db.core :as db]
   [ashigaru-health.patients.spec :as patients-spec]
   [ashigaru-health.fixtures :as fixtures]
   [ashigaru-health.test-db :as test-db]
   [ashigaru-health.test-utils
    :as test-utils
    :refer [simple-request
            with-json-body
            simple-json-request]]
   ; [clj-test-containers.core :as containers]
   [clojure.spec.alpha :as s]
   [clojure.string :as string]
   [midje.experimental :refer [for-all]]
   [midje.sweet
    :as midje
    :refer [=> =not=> fact]]
   [orchestra.spec.test]
   [ring.mock.request :as mock]))

(orchestra.spec.test/instrument)
(db/setup-all!)
(db-coerce/coerce-all!)

(def container (test-db/start-test-container!))

(defn get-test-app!
  []
  (let [test-db (test-db/get-test-db! container fixtures/patients)
        db (atom fixtures/patients)
        app (get-app {:db db
                      :logging? false
                      :connection-pool test-db})]
    app))

(defn build-patients-path
  [& parts]
  (string/join "/" (into ["/patients"] parts)))

(defn no-id
  [patient]
  (dissoc patient :id))

(defn get-unknown-id
  [patients]
  (->> patients
       (map :id)
       (apply max)
       inc))
; (midje/namespace-state-changes
;  [(midje/after :contents (containers/perform-cleanup!))])

(fact "`GET /patients` returns list of patients"
      (let [app (get-test-app!)
            response (simple-json-request app :get (build-patients-path))
            status (:status response)
            prepared-patients (->> response
                                   :body
                                   (map no-id)
                                   (into #{}))]
        status => 200
        prepared-patients => fixtures/patients))

(let [app (get-test-app!)]
  (for-all
   [body (s/gen ::patients-spec/patient-no-id)]

   (fact "`POST /patients`creates new patient"
         (let [path (build-patients-path)
               response (-> (mock/request :post path)
                            (mock/json-body body)
                            app
                            with-json-body)
               location (get-in response [:headers "Location"])
               location-response (simple-json-request app :get location)]
           (when-not (= (:status response) 409)
             (:status response) => 201
             location =not=> nil
             (no-id (:body response)) => body
             (:status location-response) => 200
             (:body location-response) => (:body response))))

   (midje/future-fact "`POST /patients` handles oms uniqueness constraint"
                      (let [path (build-patients-path)
                            response (-> (mock/request :post path)
                                         (mock/json-body body)
                                         app)]
                        (:status response) => 409))))

(fact "`POST /patients` validates incoming data"
      (let [app (get-test-app!)
            path (build-patients-path)
            body {:first_name nil
                  :last_name 123
                  :patronim true
                  :address ""
                  :gender "abc"
                  :oms "12345"}
            response (-> (mock/request :post path)
                         (mock/json-body body)
                         app)]
        (:status response) => 400))

(midje/tabular
 (fact "`/patients` not allowed methods return 405"
       (let [app (get-test-app!)
             response (simple-request app ?method (build-patients-path))]
         (:status response) => 405))
 ?method
 :delete
 :put
 :patch)

(let [app (get-test-app!)
      patients (:body (simple-json-request app :get (build-patients-path)))
      unknown-id (get-unknown-id patients)]
  (fact "`GET /patients/:id` returns patient info"
        (doseq [patient patients]
          (let [path (build-patients-path (:id patient))
                response (simple-json-request app :get path)]
            (:status response) => 200
            (:body response) => patient)))

  (fact "`GET /patients/:id` returns not found if patient doesn't exist"
        (let [path (build-patients-path unknown-id)
              response (simple-request app :get path)]
          (:status response) => 404)))

(let [app (get-test-app!)
      patients (:body (simple-json-request app :get (build-patients-path)))]
  (fact "`PATCH /patients/:id` updates patient"
        (doseq [patient patients]
          (for-all
           [body (s/gen ::patients-spec/partial-patient)]
           (let [path (build-patients-path (:id patient))
                 response (-> (mock/request :patch path)
                              (mock/json-body body)
                              app
                              with-json-body)]
             (:status response) => 200
             (:body response) => (midje/contains body)))))

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
                           app)]
          (:status response) => 405))

  (fact "`PATCH /patients/:id` handles not found"
        (let [unknown-id (get-unknown-id patients)
              path (build-patients-path unknown-id)
              response (simple-request app :patch path)]
          (:status response) => 404))

  (midje/future-fact "`PATCH /patients/:id` handles oms uniqueness constraint"))

(let [app (get-test-app!)
      patients (:body (simple-json-request app :get (build-patients-path)))]
  (fact "`DELETE /patient/:id` deletes patients"
        (doseq [patient patients]
          (let [id (:id patient)
                path (format "/patients/%s" id)
                pre-delete-response (simple-request app :get path)
                first-delete-response (simple-request app :delete path)
                post-delete-response (simple-request app :get path)
                second-delete-response (simple-request app :delete path)]
            (:status pre-delete-response) => 200
            (:status first-delete-response) => 204
            (:status post-delete-response) => 404
            (:status second-delete-response) => 404))))

(midje/tabular
 (fact "`/patients/:id` not allowed methods return 405"
       (let [app (get-test-app!)
             response (simple-request app ?method (build-patients-path 0))]
         (:status response) => 405))
 ?method
 :put
 :post)
