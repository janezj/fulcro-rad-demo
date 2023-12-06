(ns build
  (:require [clojure.java.shell :refer [sh]]
            [clojure.tools.build.api :as b]))

(def class-dir "target/classes")
(def uber-file "target/app.jar")

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [{:keys [db-alias]}]
  ;; 
  ;; org.clojure/tools.namespace is required for the development namespace 
  ;; so we must include development dependencies in the uberjar
  ;; the intent is to bring up docker to play with the app, so dev is always included
  ;;
  (let [basis (b/create-basis {:project "deps.edn" :aliases [:dev db-alias]})
        src-dirs ["src/shared" "resources" (str "src/" (name db-alias))]]
    (println "Uber with alias db-alias: " db-alias)
    (println "src-dirs: " src-dirs)
    (clean nil)
    (println "Copy.")
    (b/copy-dir {:src-dirs src-dirs
                 :target-dir class-dir})

    (println "Compile..")
    (b/compile-clj {:basis basis
                    :src-dirs src-dirs
                    :class-dir class-dir})
    (println "Uber..." uber-file)
    (b/uber {:class-dir class-dir
             :uber-file uber-file
             :basis basis
             :manifest (into {} (filter val
                                        {"_date" (java.util.Date.)
                                         "_db_alias" (str db-alias)
                                       ;:_git_count_revs (b/git-count-revs)
                                         "hostname" (:out (sh "hostname"))
                                         "CI_COMMIT_SHA" (System/getenv "CI_COMMIT_SHA")
                                         "CI_BUILD_ID" (System/getenv "CI_BUILD_ID")
                                         "CI_PIPELINE_ID" (System/getenv "CI_PIPELINE_ID")
                                         "CI_COMMIT_BRANCH" (System/getenv "CI_COMMIT_BRANCH")}))
             :main 'devmain #_'com.example.components.server })))
