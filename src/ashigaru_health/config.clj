(ns ashigaru-health.config
  (:require [maailma.core :as maailma]
            [clojure.spec.alpha :as s]))

(s/def ::env #{"production" "development" "test"})
(s/def ::app (s/keys :req-un [::env]))

(s/def ::port int?)
(s/def ::server (s/keys :req-un [::port]))

(s/def ::config (s/keys :req-un [::app ::server]))

(defn validate-config
  [config]
  (when-not (s/valid? ::config config)
    (throw (ex-info "Invalid configuration" (s/explain-data ::config config))))
  config)

(defn load-config
  ([] (load-config nil))
  ([override]
   (let [config (maailma/build-config
                 (maailma/resource "config-defaults.edn")
                 (maailma/file ".local-config.edn")
                 (maailma/env "app")
                 (maailma/env "server")
                 (maailma/env "db")
                 (maailma/env-var "PORT" [:server :port])
                 override)]
     (update-in config [:server :port] #(Integer/parseUnsignedInt %)))))

(defn load-and-validate-config
  ([] (load-and-validate-config nil))
  ([override]
   (validate-config (load-config override))))
