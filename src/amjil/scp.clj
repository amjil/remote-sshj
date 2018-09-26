(ns amjil.scp
  (:require [amjil.strint :refer (<<)]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io])
  (:import
   (net.schmizz.sshj.common StreamCopier$Listener)
   (net.schmizz.sshj.xfer FileSystemFile TransferListener)
   (net.schmizz.sshj SSHClient)
   (net.schmizz.sshj.userauth.keyprovider FileKeyProvider)
   (net.schmizz.sshj.transport.verification PromiscuousVerifier)))
;; https://github.com/re-ops/re-mote

(def default-port 22)

(def ^:dynamic timeout (* 1000 60 10))

(defn sshj-client []
  (doto (SSHClient.)
    (.addHostKeyVerifier (PromiscuousVerifier.))
    (.setTimeout timeout)))

; (defn ssh-strap [{:keys [host ssh-port ssh-key user]}]
;   (doto (sshj-client)
;     (.connect host (or ssh-port default-port))
;     (.authPublickey user #^"[Ljava.lang.String;" (into-array [(or ssh-key default-key)]))))

(defn ssh-strap [{:keys [host ssh-port user password]}]
  (doto (sshj-client)
    (.connect host (or ssh-port default-port))
    (.authPassword user password)))

(defmacro with-ssh [remote & body]
  `(let [~'ssh (ssh-strap ~remote)]
     (try
       ~@body
       (catch Throwable e#
         (throw e#))
       (finally
         (log/trace "disconneted ssh")
         (.disconnect ~'ssh)))))

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
  (when-not (.exists (io/file dst))
    (throw (ex-info "file already exist" {:file dst})))
  (with-ssh remote
    (let [scp (.newSCPFileTransfer ssh)]
      (.setTransferListener scp listener)
      (.download scp src (FileSystemFile. dst)))))
