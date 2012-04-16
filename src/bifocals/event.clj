(ns bifocals.event
  (:import franklin.Ben))

(def events #{:new-user :lost-user :start-calibration :end-calibration
              :start-pose :end-pose})

(defn register
  [kinect event handler]
  (when-not (contains? events event)
    (throw (new IllegalArgumentException (str "Unknown event " event))))
  (case event
    :new-user (.onNewUser kinect handler)
    :lost-user (.onLostUser kinect handler)
    :start-calibration (.onStartCalibration kinect handler)
    :end-calibration (.onEndCalibration kinect handler)
    :start-pose (.onStartPose kinect handler)
    :end-pose (.onEndPose kinect handler))
  nil)
