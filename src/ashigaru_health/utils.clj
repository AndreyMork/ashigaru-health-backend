(ns ashigaru-health.utils
  (:require [compojure.middleware :refer [wrap-canonical-redirect remove-trailing-slash]]))

(defn method-is?
  [method]
  (comp #(= % method) :request-method :request))

(defn build-entry-url
  [request id]
  (format "%s://%s:%s%s/%s"
          (name (:scheme request))
          (:server-name request)
          (:server-port request)
          (:uri request)
          (str id)))

(defn wrap-redirect-trailing-slash
  [middleware]
  (wrap-canonical-redirect
   middleware
   #(case %
      "/" %
      (remove-trailing-slash %))))
