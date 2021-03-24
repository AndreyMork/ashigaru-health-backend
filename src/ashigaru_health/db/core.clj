(ns ashigaru-health.db.core
  (:require
   [ashigaru-health.db.coerce :as db-types-coerce]
   [ashigaru-health.db.hugsql-setup :as hugsql-setup]
   [next.jdbc :as jdbc]))

(defn setup-all!
  []
  (db-types-coerce/coerce-all!)
  (hugsql-setup/add-updates-parameter-type!))

(defn test-connection
  [connection]
  (jdbc/execute! connection ["SELECT now()"]))

(defn get-connection-pool
  [{:keys [database
           username
           password
           host
           port]}]
  (jdbc/get-datasource {:dbtype "postgresql"
                        :dbname database
                        :user username
                        :password password
                        :host host
                        :port port}))
