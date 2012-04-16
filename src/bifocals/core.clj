(ns bifocals.core
  (:import franklin.Ben)
  (:require [bifocals.event :as event]
            [bifocals.skeleton :as skel]))

;; atoms

(def kinect (atom nil))

(def users
  "Contains the uids of all on-screen users."
  (atom #{}))

(def skeletons
  "Contains the skeletons of all calibrated users, keyed by uid. Each skeleton
  is just a map whose keys are joint keywords and whose values are 3D
  coordinates (that is, a map with :x, :y, and :z keys)."
  (atom {}))


;; event handlers

(defn- track-or-retry
  [_ uid success]
  (if success
    (.startTrackingSkeleton @kinect uid)
    (.requestCalibrationSkeleton @kinect uid true)))

(defn- add-user
  [_ uid]
  (swap! users conj uid)
  (.requestCalibrationSkeleton @kinect uid true))

(defn- remove-user
  [_ uid]
  (swap! users disj uid)
  (swap! skeletons dissoc uid))


;; main api

(defn setup
  "This should be the first thing you call, before any other function in this
  namespace, or there will be NPEs. By default, initializes the kinect in mirror
  mode; pass `:mirror false` to disable this."
  [& {:keys [mirror] :or {mirror true}}]
  (let [kinect-obj (new Ben)]
    (swap! kinect (fn [_] kinect-obj)))
  (when-not (.enableDepth @kinect)
    (throw (new Error (str "Couldn't set up the kinect object! Are you sure "
                           "it's plugged in to both your computer and "
                           "power?"))))
  (when mirror
    (.setMirror @kinect true))
  (.enableUser @kinect Ben/SKEL_PROFILE_ALL)
  (event/register @kinect :new-user add-user)
  (event/register @kinect :lost-user remove-user)
  (event/register @kinect :end-calibration track-or-retry))

(defn tick []
  "This must be called in your draw method, or else the depth and scene images
  will be all black, and user/skeleton tracking will not happen."
  (.update @kinect)
  (doseq [uid @users]
    (when (.isTrackingSkeleton @kinect uid)
      (swap! skeletons assoc uid (skel/skeleton @kinect uid)))))


(defn depth-image []
  "This is the kinect's fabled depthmap image. It is a PImage, so quil will be
  able to handle it no problem, but pure clojure consumers should brace
  themselves for interop and check out the PImage javadocs at
  http://processing.googlecode.com/svn/trunk/processing/build/javadoc/core/index.html"
  (.depthImage @kinect))

(defn depth-width []
  "The width of the depth image, in pixels."
  (.depthWidth @kinect))

(defn depth-height []
  "The height of the depth image, in pixels."
  (.depthHeight @kinect))

(defn scene-image []
  "The scene image is from the kinect's color camera. It is a PImage, so quil
  will be able to handle it no problem, but pure clojure consumers should brace
  themselves for interop and check out the PImage javadocs at
  http://processing.googlecode.com/svn/trunk/processing/build/javadoc/core/index.html"
  (.sceneImage @kinect))

(defn scene-width []
  "The width of the scene image, in pixels."
  (.sceneWidth @kinect))

(defn scene-height []
  "The height of the scene image, in pixels."
  (.sceneHeight @kinect))


(defn project
  "Projects 3D coordinates into depth image coordinates."
  [coords]
  (skel/project @kinect coords))

(defn project-skeleton
  "Projects the given skeleton from 3D coordinates into depth image coordinates.
  This lets you, for example, draw the user's skeleton on the depth image."
  [skeleton]
  (into {} (for [[joint coords] (seq skeleton)]
             [joint (project coords)])))
