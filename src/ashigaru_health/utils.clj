(ns ashigaru-health.utils
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [integrant.core :as ig]
   [next.jdbc.types :as sql-types])
  (:import
   java.net.URI
   org.postgresql.util.PSQLException))

(defn build-location-url
  [request id]
  (let [scheme (name (:scheme request))
        host (:server-name request)
        port (:server-port request)
        path (:uri request)]
    (if (nil? host)
      (format "/%s/%s" path id)
      (format "%s://%s:%s%s/%s"
              scheme
              host
              port
              path
              id))))

(defn update-if-exists
  [coll key f & args]
  (if (get coll key)
    (update coll key #(apply f % args))
    coll))

(def new-patient-keys
  #{:first_name
    :last_name
    :patronim
    :birthdate
    :gender
    :address
    :oms_number})

(defn reject-extra-patient-keys
  [params]
  (select-keys params new-patient-keys))

(defn sqlize-patient
  [patient]
  (-> patient
      reject-extra-patient-keys
      (update-if-exists :gender sql-types/as-other)
      (update-if-exists :birthdate sql-types/as-date)))

(defn parse-db-uri
  [uri-string]
  (let [uri (URI/create uri-string)
        host (.getHost uri)
        port (.getPort uri)
        [username password] (string/split (.getUserInfo uri) #":")
        database (string/join (rest (.getPath uri)))]
    {:host host
     :port port
     :username username
     :password password
     :database database}))

; https://www.postgresql.org/docs/13/errcodes-appendix.html
; Class 23 — Integrity Constraint Violation
; 23505 - unique_violation
(defn postgres-unique-constraint-violation?
  [error]
  (and (instance? PSQLException error)
       (= (.getSQLState error)
          "23505")))

(defn oms-number-unique-constraint-error?
  [error]
  (let [message (ex-message error)
        constraint-name "patients_oms_number_key"]
    (and (postgres-unique-constraint-violation? error)
         (string/includes? message constraint-name))))

(defn load-system-config
  ([] (load-system-config "system.edn"))
  ([file-name] (-> file-name
                   io/resource
                   slurp
                   ig/read-string)))
