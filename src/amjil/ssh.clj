(ns amjil.ssh
  (:require [amjil.strint :refer (<<)]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io])
  (:import
   (java.util.concurrent TimeUnit)
   (net.schmizz.sshj.common StreamCopier$Listener)
   (net.schmizz.sshj.xfer FileSystemFile TransferListener)
   (net.schmizz.sshj SSHClient)
   (net.schmizz.sshj.userauth.keyprovider FileKeyProvider)
   (net.schmizz.sshj.transport.verification PromiscuousVerifier)))
;; Copied from https://github.com/re-ops/re-mote

(def default-key (<< "~(System/getProperty \"user.home\")/.ssh/id_rsa"))

(def default-user "root")
(def default-port 22)

(def ^:dynamic operation-timeout (* 1000 60 10))

(def ^:dynamic connection-timeout (* 1000 2))

(defn sshj-client []
  (doto (SSHClient.)
    (.addHostKeyVerifier (PromiscuousVerifier.))
    (.setConnectTimeout connection-timeout)
    (.setTimeout operation-timeout)))

(defn ssh-strap [{:keys [host ssh-port ssh-key user password auth-key]}]
  (try
    (doto (sshj-client)
      (.connect host (or ssh-port default-port))
      (.authPassword user password))
      ; (if (true? auth-key)
      ;   (.authPublickey user #^"[Ljava.lang.String;" (into-array [(or ssh-key default-key)]))
      ;   (.authPassword user password)))
    (catch Exception e
      (if-let [m (some-> e .getCause .getMessage)]
        (when (.contains m "Problem getting public")
          (throw (ex-info "Cannot load public key (new OPENSSH format keys aren't supported by sshj)" {:ssh-key ssh-key})))
        (throw e)))))

(defmacro with-ssh [remote & body]
  `(let [~'ssh (ssh-strap ~remote)]
     (try
       ~@body
       (catch Throwable e#
         (throw e#))
       (finally
         (log/trace "disconneted ssh")
         (.disconnect ~'ssh)))))

(defn log-output
  "Output log stream"
  [out host]
  (doseq [line (line-seq (io/reader out))]
    (log/debug (<< "[~{host}]:") line)))

(defn execute
  "Executes a cmd on a remote host"
  [cmd remote & {:keys [out-fn err-fn] :or {out-fn log-output err-fn log-output}}]
  (with-ssh remote
    (let [session (doto (.startSession ssh) (.allocateDefaultPTY)) command (.exec session cmd)]
      (try (log/trace (<< "[~(remote :host)]:") cmd)
           (out-fn (.getInputStream command) (remote :host))
           (err-fn (.getErrorStream command) (remote :host))
           (.join command 60 TimeUnit/SECONDS)
           (.getExitStatus command)
           (finally
             (.close session)
             (log/trace "session closed!"))))))

(def listener
  (proxy [TransferListener] []
    (directory [name*] (log/debug "starting to transfer" name*))
    (file [name* size]
      (proxy [StreamCopier$Listener] []
        (reportProgress [transferred]
          (log/debug (<< "transferred ~(int (/ (* transferred 100) size))% of ~{name*}")))))))

(defn upload
  [src dst remote]
  (when-not (.exists (io/file src))
    (throw (ex-info "missing source file" {:file src})))
  (with-ssh remote
    (let [scp (.newSCPFileTransfer ssh)]
      (.setTransferListener scp listener)
      (.upload scp (FileSystemFile. src) dst))))

(defn download
  [src dst remote]
  (with-ssh remote
    (let [scp (.newSCPFileTransfer ssh)]
      (.setTransferListener scp listener)
      (.download scp src (FileSystemFile. dst)))))
