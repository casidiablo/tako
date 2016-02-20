(defproject tako "0.1.0-SNAPSHOT"
  :description "tako - Github notifier for Slack"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "2.0.1"]
                 [cheshire "5.5.0"]
                 [org.julienxx/clj-slack "0.5.2.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [clojurewerkz/propertied "1.2.0"]
                 [com.amazonaws/aws-lambda-java-core "1.1.0"]]
  :main ^:skip-aot tako.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
