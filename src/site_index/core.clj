(ns site-index.core
  (:use clj-web-crawler)
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [clj-ws (client "http://86.120.215.146:8080")
        home  (method "/")] 
    (crawl clj-ws home
           (println (.getStatusCode home))  
           (println (response-str home)))))
