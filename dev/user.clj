(ns user
  (:require
   [ashigaru-health.system]
   [ashigaru-health.system-test]
   [ashigaru-health.utils :as utils]
   [clj-test-containers.core :as containers]
   [clojure.repl :refer :all]
   ; [clojure.tools.namespace.repl :refer [set-refresh-dirs]]
   ; [integrant.core :as ig]
   [integrant.repl :as ig-repl]))

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
  nil)
