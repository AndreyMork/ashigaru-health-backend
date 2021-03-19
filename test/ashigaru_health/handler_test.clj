(ns ashigaru-health.handler-test
  (:require
   [ashigaru-health.handler :refer [get-app]]
   [ashigaru-health.specs.patient :as patient-spec]
   [ashigaru-health.test-checkers
    :refer [with-status?
            with-body?
            with-body-without-id?]]
   [ashigaru-health.test-utils
    :as test-utils
    :refer [simple-request
            with-json-body
            simple-json-request]]
   [clojure.string :as string]
   [clojure.spec.alpha :as s]
   [midje.experimental :refer [for-all]]
   [midje.sweet
    :as midje
    :refer [=> =not=> fact]]
   [orchestra.spec.test]
   [ring.mock.request :as mock]))

(orchestra.spec.test/instrument)

(defn test-setup
  ([] (test-setup 10))
  ([n]
   (let [patients (test-utils/generate-patients-map n)
         db (atom patients)
         app (get-app {:db db
                       :logging? false})]
     {:app app
      :patients patients})))

(defn build-patients-path
  [& parts]
  (string/join "/" (into ["/patients"] parts)))

(let [{:keys [app patients]} (test-setup)]
  (fact "`GET /patients` returns list of patients"
        (let [response (simple-json-request app :get "/patients")]
          response => (with-body? (vals patients))
          response => (with-status? 200))))

(let [{:keys [app]} (test-setup 0)]
  (for-all
   [body (s/gen ::patient-spec/patient-no-id)]
   (fact "`POST /patients`creates new patient"
         (let [path (build-patients-path)
               response (-> (mock/request :post path)
                            (mock/json-body body)
                            app
                            with-json-body)
               location (get-in response [:headers "Location"])
               response-body (:body response)
               location-response (simple-json-request app :get location)]
           response => (with-status? 201)
           location =not=> nil
           response => (with-body-without-id? body)
           location-response => (with-status? 200)
           location-response => (with-body? response-body)))))

(midje/future-fact "`POST /patients` handles oms uniqueness constraint"
                   (let [app identity
                         body nil
                         path (build-patients-path)
                         response (-> (mock/request :post path)
                                      (mock/json-body body)
                                      app)]
                     response => (with-status? 409)))

(fact "`POST /patients` validates incoming data"
      (let [{:keys [app]} (test-setup 0)
            path (build-patients-path)
            body {:first-name nil
                  :last-name 123
                  :patronim true
                  :address ""
                  :gender "abc"
                  :oms "12345"}
            response (-> (mock/request :post path)
                         (mock/json-body body)
                         app)]
        response => (with-status? 400)))

(midje/tabular
 (fact "`/patients` not allowed methods return 405"
       (let [{:keys [app]} (test-setup)
             response (simple-request app ?method (build-patients-path))]
         response => (with-status? 405)))
 ?method
 :delete
 :put
 :patch)

(let [{:keys [app patients]} (test-setup 10)]
  (fact "`GET /patients/:id` returns patient info"
        (doseq [id (keys patients)]
          (let [path (build-patients-path id)
                response (simple-json-request app :get path)]
            response => (with-body? (get patients id))
            response => (with-status? 200))))

  (fact "`GET /patients/:id` returns not found if patient doesn't exist"
        (let [id (test-utils/generate-unkonwn-id patients)
              path (build-patients-path id)
              response (simple-request app :get path)]
          response => (with-status? 404))))

(let [{:keys [app patients]} (test-setup 10)]
  (doseq [id (keys patients)]
    (for-all
     [body (s/gen ::patient-spec/partial-patient)]
     (fact "`PATCH /patients/:id` updates patient"
           (let [path (build-patients-path id)
                 response (-> (mock/request :patch path)
                              (mock/json-body body)
                              app
                              with-json-body)
                 response-body (:body response)]
             response => (with-status? 200)
             response-body => (midje/contains body)))))

  (fact "`PATCH /patients/:id` handles not found"
        (let [id (test-utils/generate-unkonwn-id patients)
              path (build-patients-path id)
              response (simple-request app :patch path)]
          response => (with-status? 404))))

(fact "`PATCH /patients/:id` validates incoming data"
      (let [{:keys [app]} (test-setup 0)
            path (build-patients-path)
            body {:first-name nil
                  :last-name 123
                  :patronim true
                  :address ""
                  :gender "abc"
                  :oms "12345"}
            response (-> (mock/request :post path)
                         (mock/json-body body)
                         app)]
        response => (with-status? 400)))

(let [{:keys [app patients]} (test-setup 10)]
  (fact "`DELETE /patient/:id` deletes patients"
        (doseq [id (keys patients)]
          (let [path (format "/patients/%s" id)
                pre-delete-response (simple-request app :get path)
                first-delete-response (simple-request app :delete path)
                post-delete-response (simple-request app :get path)
                second-delete-response (simple-request app :delete path)]
            pre-delete-response => (with-status? 200)
            first-delete-response => (with-status? 204)
            post-delete-response => (with-status? 404)
            second-delete-response => (with-status? 404)))))

(midje/tabular
 (fact "`/patients/:id` not allowed methods return 405"
       (let [{:keys [app]} (test-setup 0)
             response (simple-request app ?method (build-patients-path 0))]
         response => (with-status? 405)))
 ?method
 :put
 :post)
