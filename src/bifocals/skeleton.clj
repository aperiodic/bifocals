(ns bifocals.skeleton
  (:import franklin.Ben
           processing.core.PVector))

(def joints {:head Ben/SKEL_HEAD
             :neck Ben/SKEL_NECK
             :left-shoulder Ben/SKEL_LEFT_SHOULDER
             :right-shoulder Ben/SKEL_RIGHT_SHOULDER
             :left-elbow Ben/SKEL_LEFT_ELBOW, :right-elbow Ben/SKEL_RIGHT_ELBOW
             :left-hand Ben/SKEL_LEFT_HAND, :right-hand Ben/SKEL_RIGHT_HAND
             :torso Ben/SKEL_TORSO
             :left-hip Ben/SKEL_LEFT_HIP, :right-hip Ben/SKEL_RIGHT_HIP
             :left-knee Ben/SKEL_LEFT_KNEE, :right-knee Ben/SKEL_RIGHT_KNEE
             :left-foot Ben/SKEL_LEFT_FOOT, :right-foot Ben/SKEL_RIGHT_FOOT})

(defn- pvec->map
  [^PVector pvec]
  {:x (.x pvec), :y (.y pvec), :z (.z pvec)})

(defn skeleton
  "Retrieves the skeleton for the user with the given uid. If that user's
  skeleton is being tracked, then this will return a map whose keys are joint
  keywords and whose values are maps corresponding to the 3D coordinates of the
  joints. The maps have :x, :y, and :z keys, and floats for values."
  [kinect uid]
  (when (.isTrackingSkeleton kinect uid)
    (let [pvecs (zipmap (keys joints)
                        (repeatedly (count joints) #(new PVector)))]
      ; yeah, the pvecs are mutated here. it's gross.
      (doseq [[joint joint-id] joints]
        (.getJointPositionSkeleton kinect uid joint-id (get pvecs joint)))
      ; convert the pvectors into maps
      (into {} (for [[joint pvec] pvecs] [joint (pvec->map pvec)])))))

(defn- pvec->2map
  [^PVector pvec]
  {:x (.x pvec), :y (.y pvec)})

(defn project
  "Project a skeleton joint position from the 3D coordinates into depth image
  coordinates. This lets you, for example, draw the user's skeleton on top of
  the depth image."
  [kinect {:keys [x y z]}]
  (let [world-pvec (new PVector x y z)
        screen-pvec (new PVector)]
    ; more mutation, this time of screen-pvec
    (.convertRealWorldToProjective kinect world-pvec screen-pvec)
    (pvec->2map screen-pvec)))
