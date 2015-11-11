(ns my-fx.core
  (:gen-class)
  (:require [fx-clj.core :as fx]
            [taoensso.timbre :as log]
            [taoensso.timbre.appenders.core :as appenders]
            [my-fx.my-run :as my-run])
  (:import (javafx.stage Modality)
           (java.awt SplashScreen)))

(defonce app-state (atom {:output-to   nil
                      :source-file nil}))

(log/merge-config! {:appenders {:println {:enabled? true}
                                :spit (appenders/spit-appender {:fname "fx.log"})}})

(log/set-level! :debug)

(defn create-grid []
  (let [grid              (fx/grid-pane {:alignment (javafx.geometry.Pos/CENTER)
                                         :hgap      10
                                         :vgap      10
                                         :padding   (javafx.geometry.Insets. 25 25 25 25)})
        text-field        (fx/text-field)
        label             (fx/label {:text "Your Message"})
        btn               (fx/button {:text "Say Hello"})
        dir-label         (fx/label {:text ""})
        dir-btn           (fx/button {:text "Choose directory"
                                      :on-action (fn [e] 
                                                   (let [dir-c      (fx/pset! (javafx.stage.DirectoryChooser.))
                                                         output-dir (.showDialog dir-c (.getParent grid))]
                                                     (when output-dir
                                                       (swap! app-state assoc :output-to (.getAbsolutePath output-dir))
                                                       )))})
        file-chosen-label (fx/label {:text ""})
        file-choose-btn   (fx/button {:text "Choose file"
                                      :on-action (fn [e]
                                                   (let [file-chooser (fx/pset! (javafx.stage.FileChooser.))
                                                         file-chosen  (.showOpenDialog file-chooser (.getParent grid))]
                                                     (when file-chosen
                                                       (swap! app-state assoc :source-file file-chosen))))})]
    
    ;; Watch for changes to the application state.
    (add-watch app-state 
               :app-state
               (fn [k a old-state new-state]
                 ;;Display the directory chosen where we want to save our file.
                 (when (:output-to new-state)
                   (fx/pset! dir-label {:text (:output-to new-state)}))
                 ;;Display the file we selected
                 (when (:source-file new-state)
                   (fx/pset! file-chosen-label {:text (.getCanonicalPath (:source-file new-state))}))))


    (.add grid label 0 1)
    (.add grid text-field 1 1)
    (.add grid dir-btn 0 2)
    (.add grid dir-label 1 2)
    (.add grid file-choose-btn 0 3)
    (.add grid file-chosen-label 1 3)
    (.add grid btn 1 5 2 2)
    grid))

(defn start-app
  [app-fn & {:keys [title maximized]}]
  (try
    (my-run/run<!!
     (let [scene (fx/scene (app-fn))
           stage (fx/stage)]
       (.setScene stage scene)
       (.initModality stage Modality/NONE)
       (fx/pset! stage {:title title})
       (when maximized (.setMaximized stage true))
       (.show stage)
       (let [splash-screen (SplashScreen/getSplashScreen)]
         (when splash-screen
           (try
             (.close splash-screen)
             (catch Exception ex))))
       stage))
    (catch Exception e (log/info (.getMessage e)))))


(defn -main [& args]
  (log/info "STARTING....")
  (start-app create-grid :title "My Fx"))

