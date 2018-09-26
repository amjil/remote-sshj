(defproject remote-sshj "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[ch.qos.logback/logback-classic "1.2.3"]
                 [org.clojure/clojure "1.9.0"]
                 [com.hierynomus/sshj "0.26.0" :exclusions [org.slf4j/slf4j-api]]
                 [org.clojure/tools.cli "0.4.1"]
                 [org.clojure/tools.logging "0.4.1"]]

  :plugins [[lein-ancient "0.6.15"]]

  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
