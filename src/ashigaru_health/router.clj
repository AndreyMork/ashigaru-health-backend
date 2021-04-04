(ns ashigaru-health.router
  (:require
   [ashigaru-health.utils :as utils]
   [muuntaja.core :as muuntaja]
   [reitit.coercion.malli :as coercion.malli]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as rrc]
   [reitit.ring.middleware.exception :as middleware.exception]
   [reitit.ring.middleware.muuntaja :as middleware.muuntaja]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [ring.logger.timbre :as logger]
   [ring.middleware.cors :as cors]
   [ring.util.http-response :as res])
  (:import
   org.postgresql.util.PSQLException))

(defn postgres-error-handler
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch PSQLException e
        (if (utils/oms-number-unique-constraint-error? e)
          (res/conflict {:message "Provided oms number already exists"})
          (throw e))))))

(defn cors-middleware
  [origin]
  (fn
    [handler]
    (cors/wrap-cors handler
                    :access-control-allow-origin [(re-pattern origin)]
                    :access-control-allow-methods #{:get :patch :post :delete}
                    :access-control-allow-credentials "true"
                    :access-control-allow-headers #{:accept :content-type})))

(def general-routes
  [""
   ["/" {:get (fn [_] (res/ok {:message "ok"}))}]
   ["/healthz" {:get (fn [_] (res/ok {:message "healthy"}))}]
   ["/swagger.json" {:get (swagger/create-swagger-handler)}]
   ["/api-docs/*" {:get (swagger-ui/create-swagger-ui-handler)
                   :no-doc true}]])

(def muuntaja-instance
  (muuntaja/create
   (muuntaja/select-formats
    muuntaja/default-options
    ["application/json"])))

(defn get-router
  [routes config]
  (let [app-config (get config :app)
        logging? (get app-config :logging? true)
        origin (get-in app-config [:cors :origin] "")
        middlewares [middleware.muuntaja/format-middleware
                     middleware.exception/exception-middleware
                     (if logging?
                       logger/wrap-with-logger
                       identity)
                     postgres-error-handler
                     rrc/coerce-exceptions-middleware
                     rrc/coerce-request-middleware
                     rrc/coerce-response-middleware
                     (cors-middleware origin)]]

    (ring/router
     [routes general-routes]
     {:data {:muuntaja muuntaja-instance
             :coercion coercion.malli/coercion
             :middleware middlewares}})))
