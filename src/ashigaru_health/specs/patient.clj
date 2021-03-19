(ns ashigaru-health.specs.patient
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as string]))

(defn oms-generator
  []
  (gen/fmap string/join (gen/vector (gen/elements "0123456789") 16)))

(defn oms?
  [string]
  (re-matches #"\d{16}" string))

(s/def ::name? (s/with-gen
                 (s/and string? #(not= "" %))
                 (fn [] (gen/not-empty (gen/string-alphanumeric)))))
(s/def ::oms? (s/with-gen oms? oms-generator))

(s/def ::id nat-int?)
(s/def ::first-name ::name?)
(s/def ::last-name ::name?)
(s/def ::patronim (s/nilable ::name?))
(s/def ::birthdate ::name?)
(s/def ::gender #{"female" "male" "other"})
(s/def ::address ::name?)
(s/def ::oms ::oms?)

(s/def ::patient-no-id
  (s/keys :req-un [::first-name
                   ::last-name
                   ::patronim
                   ::birthdate
                   ::gender
                   ::address
                   ::oms]))

(s/def ::partial-patient
  (s/keys :opt-un [::first-name
                   ::last-name
                   ::patronim
                   ::birthdate
                   ::gender
                   ::address
                   ::oms]))

(s/def ::patient
  (s/merge (s/keys :req-un [::id])
           ::patient-no-id))

(s/def ::maybe-patient
  (s/nilable ::patient))

(s/def ::patients
  (s/coll-of ::patient))
