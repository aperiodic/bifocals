(defproject bifocals "0.1.0"
  :description "A kinect library for quil."
  :url "http://github.com/aperiodic/bifocals"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojars.processing-core/org.processing.core "1.5.1"]
                 [simple-open-ni "0.27.0"]]
  :dev-dependencies [[quil "1.0.0"]]
  :extra-classpath-dirs ["examples"]
  :java-source-path "src/java/franklin"
  :jar-exclusions [#"\.swp$" #"\.swo$" #"\.java"])
