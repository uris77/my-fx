(defproject my-fx "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [fx-clj "0.2.0-alpha1"]]

  :uberjar-name "my-fx.jar"

  :profiles {:uberjar {:aot :all
                       :omit-source true
                       :main my-fx.core}})
