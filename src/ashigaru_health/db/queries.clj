(ns ashigaru-health.db.queries
  (:require [hugsql.core :as hugsql]
            [hugsql.parameters]
            [hugsql.adapter.next-jdbc :as next-adapter]
            [next.jdbc.result-set :as result-set]))

(def adapter
  (next-adapter/hugsql-adapter-next-jdbc {:builder-fn result-set/as-unqualified-maps}))

(def path-to-sql "queries.sql")

(def opts
  {:quoting :ansi
   :adapter adapter})

(hugsql/def-db-fns path-to-sql opts)
(hugsql/def-sqlvec-fns path-to-sql opts)
