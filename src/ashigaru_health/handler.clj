(ns ashigaru-health.handler
  (:require
   [ashigaru-health.patients :as patients]
   [compojure.coercions :refer [as-int]]
   [compojure.core :refer [defroutes GET ANY]]
   [compojure.route :as route]
   [compojure.middleware :refer [wrap-canonical-redirect remove-trailing-slash]]
   [liberator.core :refer [resource]]
   [liberator.representation]
   [ring.middleware.json :refer [wrap-json-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.logger :as logger]))

(defn build-entry-url
  [request id]
  (format "%s://%s:%s%s/%s"
          (name (:scheme request))
          (:server-name request)
          (:server-port request)
          (:uri request)
          (str id)))

(defroutes app-routes
  (GET "/" [] "Hello World")

  (ANY "/patients"
    []
    (resource
     :available-media-types ["application/json"]
     :allowed-methods [:get :post]
     :location #(build-entry-url (:request %) ((comp :id ::patient) %))
     :post! (fn [{:keys [request]}] {::patient (patients/new-patient (:params request))})
     :handle-created ::patient
     :handle-ok (fn [_] (patients/get-patients))))

  (ANY "/patients/:id"
    [id :<< as-int]
    (resource :available-media-types ["application/json"]
              :allowed-methods [:get :delete :patch]
              :delete! (fn [_] (patients/delete-patient-by-id id))
              :patch! (fn [{:keys [request]}] (patients/update-patient id (:params request)))
              :exists? (fn [_]
                         (when-let [patient (patients/get-patient-by-id id)]
                           {::patient patient}))
              :handle-ok ::patient
              :handle-not-found {:message "PATIENT_NOT_FOUND"
                                 :patient-id id}))

  (route/not-found "Not Found"))

(defn wrap-redirect-trailing-slash
  [middleware]
  (wrap-canonical-redirect
   middleware
   #(case %
      "/" %
      (remove-trailing-slash %))))

(def app
  (-> app-routes
      wrap-redirect-trailing-slash
      logger/wrap-with-logger
      wrap-keyword-params
      wrap-json-params
      wrap-params))
