(ns ashigaru-health.test-utils
  (:require
   [ashigaru-health.db.queries :as queries]
   [ashigaru-health.utils :as utils]
   [clojure.string :as string]
   [integrant.core :as ig]
   [muuntaja.core :as muuntaja]
   [ring.mock.request :as mock]))

(def test-config
  {:test/fixtures {}
   :test/container {:config (ig/ref :config/load)}
   :test/connection-pool {:config (ig/ref :config/load)
                          :container (ig/ref :test/container)
                          :fixtures (ig/ref :test/fixtures)}})

(defn load-test-system-config
  []
  (let [base-config (utils/load-system-config)]
    (derive :test/connection-pool :db/connection-pool)
    (-> base-config
        (dissoc :db/connection-pool)
        (dissoc :app/server)
        (merge test-config))))

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

(defn content-type-header-set?
  [response]
  (get-in response [:headers "Content-Type"]))

(defn with-decoded-body
  [response]
  (if (content-type-header-set? response)
    (assoc response :body (muuntaja/decode-response-body response))
    response))

(defn request
  ([app method path] (request app method path nil))
  ([app method path body]
   (let [request (-> (mock/request method path)
                     (#(if body (mock/json-body % body) %)))
         response (app request)]
     (with-decoded-body response))))
