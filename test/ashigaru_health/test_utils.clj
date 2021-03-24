(ns ashigaru-health.test-utils
  (:require [ashigaru-health.patients.spec :as patient-spec]
            [clojure.data.json :as json]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [ring.mock.request :as mock]))

(defn parse-json
  [input]
  (json/read-str input :key-fn keyword))

(defn with-json-body
  [response]
  (update response :body parse-json))

(defn simple-request
  [app method path]
  (-> (mock/request method path)
      app))

(defn simple-json-request
  [app method path]
  (-> (simple-request app method path)
      with-json-body))

(defn generate-patients-vector
  [n]
  (gen/generate (-> (s/gen ::patient-spec/patient-no-id)
                    (gen/vector n))))

(defn generate-patients-map
  [n]
  (let [patients (generate-patients-vector n)]
    (zipmap (map :id patients) patients)))

(defn generate-unkonwn-id
  [patients]
  (gen/generate (gen/such-that
                 #(not (contains? patients %))
                 (s/gen nat-int?))))

