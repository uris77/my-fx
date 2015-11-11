(ns my-fx.my-run
   (:require
    [clojure.core.async :refer [go put! chan <!]]
    [taoensso.timbre :as log])
  (:refer-clojure :exclude [run!])
  (:import (javafx.application Platform)))

(defn run!* [f]
  (if (Platform/isFxApplicationThread)
    (f)
    (Platform/runLater f)))

(defmacro run!
  "Runs the enclosed body asynchronously on the JavaFX application thread
  (if caller is not already on this thread). Does not block the calling thread."
  [& body]
  `(my-fx.my-run/run!* (fn [] ~@body)))

(defn async-run-wrapper [f ch]
  (fn []
    (let [res
          (try
            (f)
            (catch Throwable ex
              (do
                (put! ch (ex-info (str "Error on JavaFX application thread") {:cause ex} ex))
                (log/info (str "Error on JavaFX application thread " (.getMessage ex)))
                (throw ex))                                    ;; Should this be rethrown??
              ))]
      (put! ch (if (nil? res) ::nil res)))))

(defn process-async-res [res]
  (cond
    (instance? Throwable res)
    (do (log/info "Throwing res " res) (throw res))

    (= ::nil res)
    nil

    :default
    res))

(defn- run<* [take-fn body]
 `(if (javafx.application.Platform/isFxApplicationThread)
    (do ~@body)
    (let [ch# (clojure.core.async/chan)]
      (javafx.application.Platform/runLater
        (my-fx.my-run/async-run-wrapper (fn [] ~@body) ch#))
      (my-fx.my-run/process-async-res (~take-fn ch#)))))

(defmacro run<!
  "Runs the enclosed body asynchronously on the JavaFX application thread
  from within a core.async go block. Returns the value of the evaluated body
  using a core.async chan and the <! function. Must be called from within a
  core.async go block!"
  [& body]
  (run<* 'clojure.core.async/<! body))

(defmacro run<!!
  "Runs the enclosed body asynchronously on the JavaFX application thread.
  Blocks the calling thread until asynchronous execution is complete and
  returns the result of the evaluated block to the caller."
  [& body]
  (run<* 'clojure.core.async/<!! body))

