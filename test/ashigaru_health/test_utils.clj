(ns ashigaru-health.test-utils
  (:require [clojure.data.json :as json]
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
