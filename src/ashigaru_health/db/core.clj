(ns ashigaru-health.db.core
  (:require
   [next.jdbc :as jdbc]))

(defn test-connection
  [connection]
  (jdbc/execute! connection ["SELECT now()"])
  connection)

(defn get-connection-pool
  [{:keys [database
           username
           password
           host
           port]}]
  (jdbc/get-datasource
   {:dbtype "postgresql"
    :dbname database
    :user username
    :password password
    :host host
    :port port}))
