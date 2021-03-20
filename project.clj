(defproject ashigaru-health "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :main ^:skip-aot ashigaru-health.main
  :dependencies [[org.clojure/clojure "1.10.2"]
                 [metosin/maailma "1.1.0"]
                 [org.clojure/test.check "1.1.0"]
                 [ring/ring-core "1.8.2"]
                 [ring/ring-json "0.5.0"]
                 [ring/ring-jetty-adapter "1.8.2"]
                 [ring-logger "1.0.1"]
                 [liberator "0.15.2"]
                 [orchestra "2021.01.01-1"]
                 [compojure "1.6.1"]]
  :plugins [[lein-cljfmt "0.7.0"]
            [lein-midje "3.2.1"]
            [lein-cloverage "1.2.2"]]
  :aliases {"rebl" ["trampoline" "run" "-m" "rebel-readline.main"]}
  :profiles {:dev {:dependencies [[clj-kondo "RELEASE"]
                                  [midje "1.9.10"]
                                  [ring/ring-mock "0.3.2"]
                                  [com.bhauman/rebel-readline "0.1.4"]]
                   :aliases {"clj-kondo" ["run" "-m" "clj-kondo.main"]}}
             :uberjar {:uberjar-name "app.jar"
                       :aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
  :cljfmt {:remove-multiple-non-indenting-spaces? true
           :split-keypairs-over-multiple-lines? true
           :paths ["src" "test" "project.clj" "resources"]})
