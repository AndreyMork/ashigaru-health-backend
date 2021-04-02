(ns ashigaru-health.kaocha-hooks
  (:require
   [ashigaru-health.system]
   [ashigaru-health.test-system]
   [ashigaru-health.test-utils :as test-utils]
   [integrant.core :as ig]))

(def loaded (atom false))

(def system-config (test-utils/load-test-system-config))
(def system (atom nil))
(def app (atom nil))

(defn loaded-on!
  []
  (reset! loaded true))

(defn loaded-off!
  []
  (reset! loaded false))

(defn set-system!
  [new-system]
  (reset! system new-system)
  (reset! app (:app/handler @system)))

(defn init-system!
  []
  (set-system! (ig/init system-config)))

(defn suspend-system!
  []
  (ig/suspend! @system))

(defn resume-system!
  []
  (set-system! (ig/resume system-config @system)))

(defn halt-system!
  []
  (ig/halt! @system)
  (loaded-off!))

(defn pre-run
  [config]
  (when-not @loaded
    (init-system!)
    (loaded-on!))
  config)

(defn pre-test
  [testable test-plan]
  (resume-system!)
  testable)

(defn post-test
  [testable test-plan]
  (suspend-system!)
  testable)
