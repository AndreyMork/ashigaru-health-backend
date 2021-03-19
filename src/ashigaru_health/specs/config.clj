(ns ashigaru-health.specs.config
  (:require [clojure.spec.alpha :as s]))

(s/def ::db some?)
(s/def ::logging? boolean?)

(s/def ::config
  (s/keys :req-un [::db
                   ::logging?]))
