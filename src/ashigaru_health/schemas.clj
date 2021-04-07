(ns ashigaru-health.schemas
  (:require
   [clojure.data.generators :as data-gen]
   [clojure.spec.gen.alpha :as gen]
   [malli.generator]
   [malli.util]
   [tick.alpha.api :as tick])
  (:import
   java.time.LocalDate))

(def valid-name-regex #"(?u)[\pL\pP ]*[\pL\pP][\pL\pP ]*")
(def valid-name-schema
  [:re
   {:gen/gen (malli.generator/generator
              [:re #"[A-Za-z\-',. ]*[A-Za-z\-',.][A-Za-z\-',. ]*"])}
   valid-name-regex])

(def oms-number-schema
  [:re #"\d{16}"])

(defn generate-date
  []
  (tick/date (data-gen/date)))

(def date-generator
  (gen/fmap
   (fn [_] (generate-date))
   (gen/return nil)))

(def date-string-generator
  (gen/fmap str date-generator))

(def date-schema
  [:or
   {:gen/gen date-string-generator}
   [:fn #(instance? LocalDate %)]
   [:fn #(try (tick/date %)
              true
              (catch Exception _
                false))]])

(def gender-schema
  [:enum "female" "male" "other"])

(def patient-schema
  [:map
   [:id nat-int?]
   [:first_name valid-name-schema]
   [:last_name valid-name-schema]
   [:birthdate date-schema]
   [:address valid-name-schema]
   [:gender gender-schema]
   [:oms_number oms-number-schema]])

(def patients-schema
  [:sequential patient-schema])

(def new-patient-schema
  (malli.util/dissoc patient-schema :id))

(def partial-patient-schema
  [:and
   (malli.util/optional-keys new-patient-schema)
   [:fn #(not= % {})]])

(comment
  (malli.generator/generate valid-name-schema)
  (malli.generator/generate new-patient-schema)
  nil)
