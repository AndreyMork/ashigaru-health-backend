(ns ashigaru-health.handler
  (:require
   [ashigaru-health.patients :as patients]
   [ashigaru-health.utils :as utils]
   [compojure.coercions :refer [as-int]]
   [compojure.core :as composure :refer [GET ANY]]
   [compojure.route :as route]
   [liberator.core :as liberator]
   [ring.middleware.json :as middleware.json]
   [ring.middleware.keyword-params :as middleware.keyword-params]
   [ring.middleware.params :as middleware.params]
   [ring.logger :as logger]))

(defn get-router
  [db]
  (composure/routes
   (GET "/" [] "Hello World")

   (ANY "/patients"
     []
     (liberator/resource
      :available-media-types ["application/json"]
      :allowed-methods [:get :post]
      :location (fn
                  [{:keys [request ::patient]}]
                  (utils/build-entry-url request (:id patient)))
      :post! (fn
               [{:keys [request]}]
               {::patient (patients/new-patient db (:params request))})
      :handle-created ::patient
      :handle-ok (fn
                   [_]
                   (patients/get-patients db))))

   (ANY "/patients/:id"
     [id :<< as-int]
     (liberator/resource :available-media-types ["application/json"]
                         :allowed-methods [:get :delete :patch]
                         :respond-with-entity? (utils/method-is? :patch)
                         :delete! (fn
                                    [_]
                                    (patients/delete-patient-by-id db id))
                         :patch! (fn
                                   [{:keys [request]}]
                                   (let [{params :params} request
                                         patient (patients/update-patient db id params)]
                                     {::patient patient}))
                         :exists? (fn
                                    [_]
                                    (when-let [patient (patients/get-patient-by-id db id)]
                                      {::patient patient}))
                         :handle-ok ::patient
                         :handle-not-found {:message "PATIENT_NOT_FOUND"
                                            :patient-id id}))

   (route/not-found "Not Found")))

(defn get-app
  [{:keys [db logging?]}]
  (let [router (get-router db)]
    (-> router
        utils/wrap-redirect-trailing-slash
        ((if logging?
           logger/wrap-with-logger
           identity))
        middleware.keyword-params/wrap-keyword-params
        middleware.json/wrap-json-params
        middleware.params/wrap-params)))
