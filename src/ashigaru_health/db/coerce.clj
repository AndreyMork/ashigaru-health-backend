(ns ashigaru-health.db.coerce
  (:require [next.jdbc.result-set :as rs])
  (:import org.postgresql.util.PGobject))

(defn SqlDate->LocalDate!
  []
  (extend-protocol rs/ReadableColumn
    java.sql.Date
    (read-column-by-label [^java.sql.Date value _] (.toLocalDate value))
    (read-column-by-index [^java.sql.Date value _2 _3] (.toLocalDate value))))

(defn Timestamp->Instant!
  []
  (extend-protocol rs/ReadableColumn
    java.sql.Timestamp
    (read-column-by-label [^java.sql.Timestamp value _] (.toInstant value))
    (read-column-by-index [^java.sql.Timestamp value _2 _3] (.toInstant value))))

(defn Citext->String!
  []
  (extend-protocol rs/ReadableColumn
    PGobject
    (read-column-by-label [^PGobject pgobj _]
      (let [type (.getType pgobj)
            value (.getValue pgobj)]
        (case type
          "citext" (str value)
          value)))
    (read-column-by-index [^PGobject pgobj _2 _3]
      (let [type (.getType pgobj)
            value (.getValue pgobj)]
        (case type
          "citext" (str value)
          value)))))

(defn coerce-all!
  []
  (SqlDate->LocalDate!)
  (Timestamp->Instant!)
  (Citext->String!))
