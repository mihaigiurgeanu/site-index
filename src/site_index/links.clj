(ns site-index.links
  (:require [clojurewerkz.crawlista.extraction.links :as links]))

;(defn with-page-meta 
;  "Adds the meta info ::page to the elements of a list"
;  [page-url links]
;  (map (fn 
;         [link] 
;         (let [link-symbol (with-meta 'link (assoc (meta 'link) ::page page-url))]
;           (eval link-symbol))) 
;       links))

(defn extract-links
  "Extracts the local links in an html adding the ::page metadata cotaining the url of
the page that the link was added"
  [html page-url]
  (links/extract-local-followable-urls html page-url))

(defn log-link 
  "Returns a string containing the page where link was found > the link"
  [link]
  link)
