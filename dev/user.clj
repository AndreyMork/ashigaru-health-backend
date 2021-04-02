(ns user
  (:require
   [ashigaru-health.system]
   [ashigaru-health.test-system]
   [ashigaru-health.utils :as utils]
   [ashigaru-health.kaocha-hooks :as kh]
   [clj-test-containers.core :as containers]
   [clojure.repl :refer :all]
   ; [clojure.tools.namespace.repl :refer [set-refresh-dirs]]
   [integrant.core :as ig]
   [integrant.repl :as ig-repl]
   [kaocha.repl :as k]
   [kaocha.watch]))

; (set-refresh-dirs "src")


(ig-repl/set-prep! #(utils/load-system-config))

(def go ig-repl/go)
(def halt ig-repl/halt)
(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)

(comment
  (go)
  (halt)
  (reset)
  (reset-all)
  (containers/perform-cleanup!)

  (k/run-all)
  (kh/init-system!)
  (kh/halt-system!)
  (kh/loaded-on!)
  (kh/loaded-off!)
  (println @kh/app)
  (println @kh/system)
  (kaocha.watch/run (kaocha.repl/config))

  nil)
