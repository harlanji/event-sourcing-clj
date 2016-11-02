(ns todos-www.routes)

(defn routes []
  (sorted-set
    ["/" :get `todos-www.backend/hello-world-handler]
    ["/index.html" :get `todos-www.backend/index-handler]))