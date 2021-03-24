(ns ashigaru-health.app
  (:require
   [ashigaru-health.patients.handler :as patients]
   [ashigaru-health.patients.spec :as patient-spec]
   [ashigaru-health.utils :as utils]
   [clojure.spec.alpha :as s]
   [compojure.coercions :refer [as-int]]
   [compojure.core :as composure :refer [GET ANY]]
   [compojure.route :as route]
   [liberator.core :as liberator]
   [ring.middleware.json :as middleware.json]
   [ring.middleware.keyword-params :as middleware.keyword-params]
   [ring.middleware.params :as middleware.params]
   [ring.logger.timbre :as logger]))

(defn get-router
  [connection-pool]
  (composure/routes
   (GET "/" [] "Hello World")

   (ANY "/patients"
     []
     (liberator/resource
      :available-media-types ["application/json"]
      :allowed-methods [:get :post]
      :malformed? (fn
                    [{:keys [request]}]
                    (let [{:keys [request-method params]} request]
                      (case request-method
                        :post (not (s/valid? ::patient-spec/patient-no-id params))
                        false)))
      :location (fn
                  [{:keys [request ::patient]}]
                  (utils/build-entry-url request (:id patient)))
      :post! (fn
               [{:keys [request]}]
               {::patient (patients/new-patient! connection-pool (:params request))})
      :handle-created ::patient
      :handle-ok (fn
                   [_]
                   (patients/get-patients connection-pool))))

   (ANY "/patients/:id"
     [id :<< as-int]
     (liberator/resource
      :available-media-types ["application/json"]
      :allowed-methods [:get :delete :patch]
      :malformed? (fn
                    [{:keys [request]}]
                    (let [{:keys [request-method params]} request]
                      (case request-method
                        :patch (not (s/valid? ::patient-spec/partial-patient params))
                        false)))
      :respond-with-entity? (utils/method-is? :patch)
      :delete! (fn
                 [_]
                 (patients/delete-patient-by-id! connection-pool id))
      :patch! (fn
                [{:keys [request]}]
                (let [{params :params} request
                      patient (patients/update-patient! connection-pool id params)]
                  {::patient patient}))
      :exists? (fn
                 [_]
                 (when-let [patient (patients/get-patient-by-id connection-pool id)]
                   {::patient patient}))
      :handle-ok ::patient
      :handle-not-found {:message "PATIENT_NOT_FOUND"
                         :patient-id id}))

   (route/not-found "Not Found")))

(defn wrap-exception-handling
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (println e)
        {:status 500
         :body "{}"}))))

(defn get-app
  [{:keys [logging? connection-pool]}]
  (let [router (get-router connection-pool)]
    (-> router
        utils/wrap-redirect-trailing-slash
        ((if logging?
           logger/wrap-with-logger
           identity))
        middleware.keyword-params/wrap-keyword-params
        middleware.json/wrap-json-params
        middleware.params/wrap-params
        wrap-exception-handling)))
