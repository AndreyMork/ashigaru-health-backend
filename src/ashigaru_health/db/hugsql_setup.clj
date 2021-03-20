(ns ashigaru-health.db.hugsql-setup
  (:require [hugsql.parameters]
            [next.jdbc.sql.builder :as sql.builder]))

(defn add-updates-parameter-type!
  []
  (defmethod hugsql.parameters/apply-hugsql-param :updates
    [param data options]
    (let [data-key (:name param)
          values-map (data data-key)
          result (sql.builder/by-keys
                  values-map
                  "" ; removes "SET" from result
                  {:column-fn #(hugsql.parameters/identifier-param-quote % options)})]
      result)))

