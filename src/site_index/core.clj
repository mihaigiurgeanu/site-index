(ns site-index.core
  (:require
    clojure.string
    [clj-http.client :as http]
    [clojurewerkz.crawlista.extraction [links :as links] [content :as content]]
    [clojurewerkz.crawlista [media :as media]]
    [site-index [links :as l]])
  (:use [clojure.tools.cli :only [cli]]
        [clojure set pprint]
        clojurewerkz.crawlista.string
        [pantomime.web]
        [site-index index links])
  (:import (de.l3s.boilerpipe.extractors DefaultExtractor KeepEverythingExtractor LargestContentExtractor NumWordsRulesExtractor))
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [[options urls doc] (cli args
                                ["-h" "--help" "Show usage info" :default false :flag true]
                                ["-p" "--path" "The path to the Lucene index folder."]
                                ["-d" "--default-extractor" "Use Boilerpipe's DefaultExtractor instead of the ArticleExtractor." :default false :flag true]
                                ["-k" "--keep-everything-extractor" "Use Boilerpipe's KeepEverythingExtractor instead of ArticleExtractor." :default false :flag true]
                                ["-l" "--largest-content-extractor" "Use Boilerpipe's LargestContentExtractor instead of ArticleExtractor." :default false :flag true]
                                ["-n" "--num-words-extractor" "Use Boilerpipe's NumWordsRulesExtractor instead of ArticleExtractor." :default false :flag true]
                                ["-1" "--test-extractor" "Do not create index, instead fetch the urls, apply content extractor and display the result." :default false :flag true]
                                ["-j" "--keep-jsessionid" "Do not drop jsessionid from the links" :default false :flag true])
       ]
    
    ;; verify command line options
    (let [extract-content (cond
                            (:default-extractor options) #(content/extract-text (DefaultExtractor/INSTANCE) %1)
                            (:keep-everything-extractor options) #(content/extract-text (KeepEverythingExtractor/INSTANCE) %1)
                            (:largest-content-extractor options) #(content/extract-text (LargestContentExtractor/INSTANCE) %1)
                            (:num-words-extractor options) #(content/extract-text (NumWordsRulesExtractor/INSTANCE) %1)
                            :default content/extract-text)
          normalize-link (comp #(clojure.string/replace %1 #"/ *$" "")
                                (if (not (:keep-jsessionid options))
                                  (do (println "Drop jsessionid") 
                                    #(clojure.string/replace %1 #";jsessionid=[ABCDEF0123456789]+" ""))
                                  (do (println "Keep jsessionid") identity)))]
      (cond
      (:help options) (println doc)
      (:test-extractor options) (let [extract-url-content (fn [url]
                                                            [url (extract-content (:body (http/get url)))])] 
                                  (doseq [[url content] (map extract-url-content urls)] (do 
                                                                                          (println "------------------------------------------------------")
                                                                                          (println url)
                                                                                          (println content))))
      (not (:path options)) (do 
                              (println "You need to specify the path to the Lucene index folder!")
                              (println doc))
      :default (index-docs
                 (:path options)
                 (loop [unprocessed-links (set (map normalize-link urls))
                        processed-links #{}
                        docs (transient [])]
                   (if (empty? unprocessed-links)
                     (persistent! docs)
                     (let [next-link (first unprocessed-links)
                           new-processed (set (conj processed-links next-link))
                           http-doc (try (http/get next-link)
                                      (catch java.lang.Exception e
                                        (println (.getMessage e) (log-link next-link))
                                        nil))]
                       (if http-doc 
                         (let [headers (:headers http-doc)
                               html (:body http-doc)
                               doc (if (media/html-content? html headers)
                                     {:url next-link
                                      :title (content/extract-title html)
                                      :content (extract-content html)}
                                     nil)
                               new-links (set (map normalize-link (extract-links html next-link)))
                               new-unprocessed (union (disj unprocessed-links next-link)
                                                      (difference new-links new-processed))
                               ]
                           (recur new-unprocessed new-processed (conj! docs doc)))
                         (recur (disj unprocessed-links next-link) new-processed docs))
                       )))
                 )))))
