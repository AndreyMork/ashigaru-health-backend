(defproject ashigaru-health "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :main ^:skip-aot ashigaru-health.main
  :dependencies [; [org.clojure/clojure "1.10.2"]
                 ; [org.clojure/data.generators "1.0.0"]
                 ; [org.postgresql/postgresql "42.2.19.jre7"]
                 ; [org.clojure/test.check "1.1.0"]
                 ; [ring/ring-core "1.8.2"]
                 ; [ring/ring-json "0.5.0"]
                 ; [ring/ring-jetty-adapter "1.8.2"]
                 ; [ring-logger-timbre "0.7.6"]
                 ; [com.fzakaria/slf4j-timbre "0.3.21"]
                 ; [com.github.seancorfield/next.jdbc "1.1.646"]
                 ; [com.layerware/hugsql-core "0.5.1"]
                 ; [com.layerware/hugsql-adapter-next-jdbc "0.5.1"]
                 ; [metosin/maailma "1.1.0"]
                 ; [tick "0.4.30-alpha"]
                 ; [liberator "0.15.2"]
                 ; [orchestra "2021.01.01-1"]
                 ; ; [hikari-cp "2.13.0"]
                 ; [compojure "1.6.1"]
                 ]
  :plugins [[lein-cljfmt "0.7.0"]
            [lein-midje "3.2.1"]
            [lein-cloverage "1.2.2"]
            [komcrad/repl-reload "0.1.2"]]
  :aliases {"rebl" ["trampoline" "run" "-m" "rebel-readline.main"]}
  :profiles {:dev {:dependencies [[clj-kondo "RELEASE"]
                                  [clj-test-containers "0.4.0"]
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
