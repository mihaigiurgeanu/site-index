(defproject site-index "0.1.0-SNAPSHOT"
  :description "Creates/updates the Lucene search index of the pages of a web-site."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clj-http "0.6.4"]
                 [clojurewerkz/crawlista "1.0.0-alpha17"]
                 [org.clojure/tools.cli "0.2.2"]
                 [org.apache.lucene/lucene-core "4.1.0"]
                 [org.apache.lucene/lucene-analyzers-common "4.1.0"]
                 ]
  :main site-index.core)
