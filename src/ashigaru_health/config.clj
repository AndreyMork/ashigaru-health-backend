(ns ashigaru-health.config
  (:require
   [ashigaru-health.utils :as utils]
   ; [clojure.spec.alpha :as s]
   [maailma.core :as maailma]))

; (s/def ::env #{"production" "development" "test"})
; (s/def ::app (s/keys :req-un [::env]))

; (s/def ::port nat-int?)
; (s/def ::logging? boolean?)
; (s/def ::join? boolean?)
; (s/def ::server (s/keys :req-un [::port ::logging? ::join?]))

; (s/def ::port nat-int?)
; (s/def ::host string?)
; (s/def ::username string?)
; (s/def ::password string?)
; (s/def ::database string?)
; (s/def ::db (s/keys :req-un [::port ::host ::username ::password ::database]))

; (s/def ::config (s/keys :req-un [::app ::server ::db]))

; (defn validate-config
;   [config]
;   (when-not (s/valid? ::config config)
;     (throw (ex-info "Invalid configuration" (s/explain-data ::config config))))
;   config)

(defn parsePort
  [port]
  (-> port
      str
      (#(Integer/parseUnsignedInt %))))

(defn load-config
  ([] (load-config nil))
  ([override]
   (let [config (maailma/build-config
                 (maailma/resource "config-defaults.edn")
                 (maailma/file ".local-config.edn")
                 (maailma/env "app")
                 (maailma/env-var "PORT" [:server :port])
                 (maailma/env-var "DATABASE_URL" [:db :uri])
                 override)]
     (-> config
         (update-in [:server :port] parsePort)
         (update :db #(if-let [uri (:uri %)] (utils/parse-db-uri uri) %))
         (update-in [:db :port] parsePort)))))

; (defn load-and-validate-config
;   ([] (load-and-validate-config nil))
;   ([override]
;    (validate-config (load-config override))))
