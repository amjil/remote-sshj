(defproject remote-sshj "0.2.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[ch.qos.logback/logback-classic "1.2.3"]
                 [org.clojure/clojure "1.10.3"]
                 [com.hierynomus/sshj "0.31.0" :exclusions [org.slf4j/slf4j-api]]
                 [org.clojure/tools.cli "1.0.206"]
                 [org.clojure/tools.logging "1.1.0"]]

  :plugins [[lein-localrepo "0.5.4"]]

  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
