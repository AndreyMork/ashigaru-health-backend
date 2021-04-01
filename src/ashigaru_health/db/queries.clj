(ns ashigaru-health.db.queries
  (:require
   [hugsql.adapter.next-jdbc :as next-adapter]
   [hugsql.core :as hugsql]
   [hugsql.parameters]
   [next.jdbc.result-set :as result-set]
   [next.jdbc.sql.builder :as sql.builder]))

(defmethod hugsql.parameters/apply-hugsql-param :updates
  [param data options]
  (let [data-key (:name param)
        values-map (data data-key)
        result (sql.builder/by-keys
                values-map
                "" ; removes "SET" from result
                {:column-fn #(hugsql.parameters/identifier-param-quote % options)})]
    result))

(def adapter
  (next-adapter/hugsql-adapter-next-jdbc {:builder-fn result-set/as-unqualified-maps}))

(def path-to-sql "queries.sql")

(def opts
  {:quoting :ansi
   :adapter adapter})

(hugsql/def-db-fns path-to-sql opts)
(hugsql/def-sqlvec-fns path-to-sql opts)
