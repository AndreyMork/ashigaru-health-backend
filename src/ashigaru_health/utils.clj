(ns ashigaru-health.utils
  (:require [compojure.middleware]
            [clojure.string :as string]
            [next.jdbc.types :as sql-types])
  (:import java.net.URI))

(defn method-is?
  [method]
  (comp #(= % method) :request-method :request))

(defn build-entry-url
  [request id]
  (format "%s://%s:%s%s/%s"
          (name (:scheme request))
          (:server-name request)
          (:server-port request)
          (:uri request)
          (str id)))

(defn wrap-redirect-trailing-slash
  [middleware]
  (compojure.middleware/wrap-canonical-redirect
   middleware
   #(case %
      "/" %
      (compojure.middleware/remove-trailing-slash %))))

(defn update-if-exists
  [coll key f & args]
  (if (get coll key)
    (update coll key #(apply f % args))
    coll))

(defn sqlize-patient
  [patient]
  (-> patient
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
