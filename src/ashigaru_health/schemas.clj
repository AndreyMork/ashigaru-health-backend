(ns ashigaru-health.schemas
  (:require
   [clojure.data.generators :as data-gen]
   [clojure.spec.gen.alpha :as gen]
   [malli.util]
   [tick.alpha.api :as tick])
  (:import
   java.time.LocalDate))

(defn contains-utf-0x00
  [x]
  (.contains x (str \u0000)))

(def not-empty-string-schema
  [:and
   [:string {:min 1}]
   [:fn #(not (contains-utf-0x00 %))]])

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
   [:first_name not-empty-string-schema]
   [:last_name not-empty-string-schema]
   [:patronim [:maybe not-empty-string-schema]]
   [:birthdate date-schema]
   [:address not-empty-string-schema]
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
