(ns ashigaru-health.handler
  (:require
   [reitit.ring :as ring]))

(defn get-handler
  [router]
  (ring/ring-handler
   router
   (ring/routes
    (ring/redirect-trailing-slash-handler)
    (ring/create-default-handler))))
