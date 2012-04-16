(ns bifocals.examples.2D
  (:require [bifocals.core :as bifocals])
  (:use quil.core))

;; An example of drawing all available skeletons, in 2D. Shows how to project
;; skeletons, which are in 3D, down to 2D. The 2D coordinates are the pixel
;; coordinates of the kinect's depth image.

(defn setup []
  (smooth)
  ; This function connects to the kinect. You must call it first. Otherwise,
  ; there will be NPEs.
  (bifocals/setup)
  (frame-rate 30))

(defn draw-line
  [p1 p2]
  (line (:x p1) (:y p1) (:x p2) (:y p2)))

(defn draw-skeleton
  [skeleton]
  (let [; This is an example of destructuring a skeleton, which is just a map
        ; with all the listed keys. The values are also maps, representing
        ; vectors, with :x, :y, and :z keys.
        {:keys [head neck left-shoulder right-shoulder left-elbow right-elbow
                left-hand right-hand torso left-hip right-hip left-knee
                right-knee left-foot right-foot]}
          ; Can also use bifocals/project to project arbitrary 3D vectors down
          ; to 2D coordinates.
          (bifocals/project-skeleton skeleton)]
    (stroke 30 120 180)
    (stroke-weight 3)
    (draw-line head neck)
    (draw-line neck left-shoulder), (draw-line neck right-shoulder)
    (draw-line left-shoulder left-elbow), (draw-line right-shoulder right-elbow)
    (draw-line left-elbow left-hand), (draw-line right-elbow right-hand)
    (draw-line left-shoulder torso), (draw-line right-shoulder torso)
    (draw-line torso left-hip), (draw-line torso right-hip)
    (draw-line left-hip left-knee), (draw-line right-hip right-knee)
    (draw-line left-knee left-foot), (draw-line right-knee right-foot)))

(defn draw []
  (background 0)
  ; You must call this in the draw function. Otherwise, your depth image will be
  ; all black, and user/skeleton tracking will not work.
  (bifocals/tick)
  (image (bifocals/image) 0 0)
  (no-stroke)
  (fill 0 255 0 128)
  ; The uids of the on-screen users are stored in a set at the
  ; `bifocals/users` atom. Note that users are added to this set as soon as they
  ; are recognized, which is before they have been calibrated, so their skeleton
  ; skeleton data may not be available. You can check if any users are in that
  ; calibrating state with `(some @bifocals/users (keys @bifocals/skeletons))`.
  (doseq [uid @bifocals/users]
    (rect 10 (- (* 30 uid) 20) 20 20))
  ; The currently tracked skeletons are stored as values of a map in the atom
  ; `bifocals/skeletons`. The map is keyed by the uids of the users that the
  ; skeletons correspond to.
  (doseq [skeleton (vals @bifocals/skeletons)]
    (draw-skeleton skeleton)))

(defsketch kinect
  :title "Can Haz Kinect!"
  :setup setup
  :draw draw
  ; If your depth image seems truncated, or the window is much larger than it,
  ; check its dimensions by calling `bifocals/depth-width` and
  ; `bifocals/depth-height`, and then adjust the sketch size accordingly.
  :size [640 480])
