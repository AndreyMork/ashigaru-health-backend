(ns ashigaru-health.handler
  (:require
   [ashigaru-health.patients :as patients]
   [ashigaru-health.utils :as utils]
   [compojure.coercions :refer [as-int]]
   [compojure.core :refer [defroutes GET ANY]]
   [compojure.route :as route]
   [liberator.core :refer [resource]]
   [liberator.representation]
   [ring.middleware.json :refer [wrap-json-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.logger :as logger]))

(defroutes app-routes
  (GET "/" [] "Hello World")

  (ANY "/patients"
    []
    (resource
     :available-media-types ["application/json"]
     :allowed-methods [:get :post]
     :location (fn
                 [{:keys [request ::patient]}]
                 (utils/build-entry-url request (:id patient)))
     :post! (fn
              [{:keys [request]}]
              {::patient (patients/new-patient (:params request))})
     :handle-created ::patient
     :handle-ok (fn
                  [_]
                  (patients/get-patients))))

  (ANY "/patients/:id"
    [id :<< as-int]
    (resource :available-media-types ["application/json"]
              :allowed-methods [:get :delete :patch]
              :respond-with-entity? (utils/method-is? :patch)
              :delete! (fn
                         [_]
                         (patients/delete-patient-by-id id))
              :patch! (fn
                        [{:keys [request]}]
                        (let [{params :params} request
                              patient (patients/update-patient id params)]
                          {::patient patient}))
              :exists? (fn
                         [_]
                         (when-let [patient (patients/get-patient-by-id id)]
                           {::patient patient}))
              :handle-ok ::patient
              :handle-not-found {:message "PATIENT_NOT_FOUND"
                                 :patient-id id}))

  (route/not-found "Not Found"))

(def app
  (-> app-routes
      utils/wrap-redirect-trailing-slash
      logger/wrap-with-logger
      wrap-keyword-params
      wrap-json-params
      wrap-params))
