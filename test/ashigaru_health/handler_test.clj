(ns ashigaru-health.handler-test
  (:require
   [ashigaru-health.handler :refer [get-app]]
   [ashigaru-health.test-checkers :refer [with-status? with-body? with-body-without-id?]]
   [ashigaru-health.test-utils
    :as test-utils
    :refer [simple-request with-json-body simple-json-request]]
   [midje.sweet :as midje :refer [=> =not=> fact]]
   [ring.mock.request :as mock]))

(def patients
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
      :oms "0000000000000000"}})

(def db
  (atom patients))

(defn reset-db
  []
  (reset! db patients))

(def app
  (get-app {:db db
            :logging? false}))

(midje/background [(midje/before :facts (reset-db))])

(fact "`GET /patients` returns list of patients"
      (let [response (simple-json-request app :get "/patients")]
        response => (with-body? (vals patients))
        response => (with-status? 200)))

(fact "`POST /patients`creates new patient"
      (let [body {:first-name "John"
                  :last-name "Doe"
                  :patronim nil
                  :birthdate "01-01-1990"
                  :gender "male"
                  :address "City"
                  :oms "1111222233334444"}
            response (-> (mock/request :post "/patients")
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
        location-response => (with-body? response-body)))

(midje/tabular
 (fact "`/patients` not allowed methods return 405"
       (simple-request app ?method "/patients") => (with-status? 405))
 ?method
 :delete
 :put
 :patch)

(fact "`GET /patients/:id` returns patient info"
      (doseq [id (keys patients)]
        (let [path (format "/patients/%s" id)
              response (simple-json-request app :get path)]
          response => (with-body? (get patients id))
          response => (with-status? 200))))

(fact "`GET /patients/:id` returns not found if patient doesn't exist"
      (let [response (simple-request app :get "/patients/3")]
        response => (with-status? 404)))

(midje/future-fact "`POST /patients` validates incoming data"
                   nil => nil)

(midje/fact "`PATCH /patients/:id` updates patient"
            (let [prepatch-response (simple-json-request app :get "/patients/1")
                  body {:first-name "patch"
                        :last-name "patch"
                        :patronim "patch"
                        :birthdate "2000"
                        :oms "1111222233334444"
                        :gender "male"
                        :address "patch"}
                  response (-> (mock/request :patch "/patients/1")
                               (mock/json-body body)
                               app
                               with-json-body)]
              prepatch-response =not=> (with-body-without-id? body)
              response => (with-status? 200)
              response => (with-body-without-id? body)))

(fact "`PATCH /patients/:id` handles not found"
      (let [response (simple-request app :patch "/patients/3")]
        response => (with-status? 404)))

(midje/future-fact "`PATCH /patients/:id` validates incoming data"
                   nil => nil)

(fact "`DELETE /patient/:id` deletes patients"
      (let [response (simple-request app :delete "/patients/2")
            second-response (simple-request app :delete "/patients/2")]
        response => (with-status? 204)
        second-response => (with-status? 404)))
