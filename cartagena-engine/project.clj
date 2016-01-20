(defproject cartagena "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [compojure "1.4.0"]]
  :target-path "target/%s"
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler cartagena.server/app}
  :profiles {:uberjar {:aot :all}}
  :test-selectors {:default (constantly true)
                   :single :single}
  :aliases {"console-game" ["run" "-m" "cartagena.console/-main"]})
