(ns site-index.index
  (:import (org.apache.lucene.analysis Analyzer)
           ;(org.apache.lucene.analysis.standard StandardAnalyzer)
           (org.apache.lucene.analysis.ro RomanianAnalyzer)
           (org.apache.lucene.document Document Field StringField TextField Field Field$Store)
           (org.apache.lucene.index IndexWriter IndexWriterConfig IndexWriterConfig$OpenMode)
           (org.apache.lucene.store SimpleFSDirectory)
           (org.apache.lucene.util Version)
           )
  (:use site-index.links
        [clojure.pprint :only [pprint]]))

(def lucene-version (Version/LUCENE_41))

(defn add-doc-to-index 
  "Uses an IndexWriter to add a document to the Lucene index"
  [^IndexWriter writer doc]
  (println "Indexing " (:url doc) ":" (:title doc) ":" (.length (:content doc)) "bytes")
  (let [lucene-doc (Document.)]
    (doseq [field [(StringField. "url" (:url doc) (Field$Store/YES))
                   (TextField. "title" (:title doc) (Field$Store/YES))
                   (TextField. "content" (:content doc) (Field$Store/YES))]]
      (.add lucene-doc field))
    (.addDocument writer lucene-doc)))

(defn index-docs
  "Creates the Lucene index for a list of documents. Each document is a map 
{:url \"url\", :title \"title\", :content \"content\"}"
  [index-location docs]
  (let [analyzer (RomanianAnalyzer. lucene-version)
        index-writer-cfg (IndexWriterConfig. lucene-version analyzer)]
    (with-open [writer (do 
                         (.setOpenMode index-writer-cfg (IndexWriterConfig$OpenMode/CREATE))
                         (IndexWriter. (SimpleFSDirectory. (java.io.File. index-location)) index-writer-cfg)
                         )]
      (doseq [doc docs] (add-doc-to-index writer doc)))
    ))
