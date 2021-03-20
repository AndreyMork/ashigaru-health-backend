(ns ashigaru-health.patients.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as string]
            [clojure.data.generators :as data-gen]
            [tick.alpha.api :as tick]))

(defn oms-number-generator
  []
  (gen/fmap string/join (gen/vector (gen/elements "0123456789") 16)))

(defn oms-number?
  [x]
  (re-matches #"\d{16}" x))

(defn generate-date
  []
  (tick/date (data-gen/date)))

; (defn generate-date-string
;   []
;   (str (generate-date)))

(def date-generator
  (gen/fmap
   (fn [_] (generate-date))
   (gen/return nil)))

(def date-string-generator
  (gen/fmap str date-generator))

(defn date?
  [x]
  (instance? java.time.LocalDate x))

(s/def ::name? (s/with-gen
                 (s/and string? #(not= "" %))
                 (fn [] (gen/not-empty (gen/string-alphanumeric)))))
(s/def ::oms-number? (s/with-gen oms-number? oms-number-generator))
(s/def ::date? (s/with-gen
                 (s/or :date date? :date-string #(re-matches #"\d{4}-\d{2}-\d{2}" %))
                 (fn [] date-string-generator)))

(s/def ::id nat-int?)
(s/def ::first_name ::name?)
(s/def ::last_name ::name?)
(s/def ::patronim (s/nilable ::name?))
(s/def ::birthdate ::date?)
(s/def ::gender #{"female" "male" "other"})
(s/def ::address ::name?)
(s/def ::oms_number ::oms-number?)

(s/def ::patient-no-id
  (s/keys :req-un [::first_name
                   ::last_name
                   ::patronim
                   ::birthdate
                   ::gender
                   ::address
                   ::oms_number]))

(s/def ::partial-patient
  (s/keys :opt-un [::first_name
                   ::last_name
                   ::patronim
                   ::birthdate
                   ::gender
                   ::address
                   ::oms_number]))

(s/def ::patient
  (s/merge (s/keys :req-un [::id])
           ::patient-no-id))

(s/def ::maybe-patient
  (s/nilable ::patient))

(s/def ::patients
  (s/coll-of ::patient))
