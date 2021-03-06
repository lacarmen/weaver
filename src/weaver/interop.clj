(ns weaver.interop
  (:require
   [clojure.tools.logging :as log]
   [clojure.java.shell :as sh]
   [clojure.java.io :as io]
   [clojure.data.json :as json]))

(defn error-log [& args] (doseq [arg args] (log/error arg)))
(defn warn-log [& args] (doseq [arg args] (log/warn arg)))
(defn info-log [& args] (doseq [arg args] (log/info arg)))
(defn debug-log [& args] (doseq [arg args] (log/debug arg)))

(defn warn-and-exit [error? & msgs]
  (log/error error?)
  (doseq [msg msgs]
    (log/error msg))
  (if (instance? java.lang.Throwable error?)
    (throw error?)
    (throw (ex-info "Encountered weaver error!"
                    {:causes (into [error?] msgs)}))))

(defn get-env
  ([key]
   (get-env (name key) nil))
  ([key not-found]
   (if-some [val (System/getenv (name key))]
     val
     not-found)))

(defn shell-exec
  ([command]
   (shell-exec
    command
    nil))
  ([command opts]
   (when (not-empty opts)
     (log/warn "Java runtime version of weaver doesn't support opts on shell-exec yet. ignoring..."))
   (let [{:keys [exit out err]} (sh/sh command)]
     (if (= exit 0)
       out
       (warn-and-exit (str "Non-zero exit code from shell-exec."
                           "\ncommand: " command
                           "\nexit-code: " exit
                           "\nstderr: " err))))))

(defn read-file [path]
  (some-> path
   (io/file)
   (slurp)))

(defn write-file [content path]
  (some-> path
   (io/file)
   (spit content)))

(defn pretty-json [obj]
  (with-out-str (json/pprint obj)))
