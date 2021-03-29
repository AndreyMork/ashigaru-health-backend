(ns ashigaru-health.test-utils
  (:require
   [ashigaru-health.db.queries :as queries]
   [ashigaru-health.utils :as utils]
   [clojure.data.json :as json]
   [clojure.string :as string]
   [integrant.core :as ig]
   [ring.mock.request :as mock])
  (:import
   java.io.ByteArrayInputStream))

(def test-config
  {:test/fixtures {}
   :test/container {:config (ig/ref :config/load)}
   :db/connection-pool {:config (ig/ref :config/load)
                        :container (ig/ref :test/container)
                        :fixtures (ig/ref :test/fixtures)}})

(defn load-test-system-config
  []
  (let [base-config (utils/load-system-config)]
    (-> base-config
        (merge test-config)
        (dissoc :app/server))))

(comment,
  (load-test-system-config)
  nil)

(defn generate-db-name
  [base]
  (str base (rand-int 1000000)))

(defn populate-test-db!
  [connection patients]
  (->> patients
       (map utils/sqlize-patient)
       (map #(assoc % :returning ["id"]))
       (run! #(queries/new-patient! connection %)))
  connection)

(defn build-patients-path
  [& parts]
  (string/join "/" (into ["/patients"] parts)))

(defn no-id
  [patient]
  (dissoc patient :id))

(defn get-unknown-id
  [patients]
  (->> patients
       (map :id)
       (apply max)
       inc))

(defn parse-json
  [input]
  (json/read-str input :key-fn keyword))

(defn with-json-body
  [response]
  (let [body (:body response)
        string-body (if (instance? ByteArrayInputStream body)
                      (slurp body)
                      body)
        json-body (parse-json string-body)]
    (assoc response :body json-body)))

(defn simple-request
  [app method path]
  (-> (mock/request method path)
      app))

(defn simple-json-request
  [app method path]
  (-> (simple-request app method path)
      with-json-body))
