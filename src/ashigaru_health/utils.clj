(ns ashigaru-health.utils
  (:require [compojure.middleware]))

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
  (compojure.middleware/wrap-canonical-redirect
   middleware
   #(case %
      "/" %
      (compojure.middleware/remove-trailing-slash %))))
