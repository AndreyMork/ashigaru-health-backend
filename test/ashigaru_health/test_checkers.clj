(ns ashigaru-health.test-checkers
  (:require
   [midje.sweet :as midje]))

(defn with-body?
  "Checks if response body is correct"
  [body]
  (midje/checker [response]
                 (= (:body response) body)))

(defn with-body-without-id?
  "Checks if response body is correct"
  [body]
  (midje/checker [response]
                 (= (dissoc (:body response) :id) body)))

(defn with-status?
  "Checks if response status is correct"
  [status]
  (midje/checker [response]
                 (= (:status response) status)))
