(defproject bifocals "0.0.1"
  :description "A kinect library for quil."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojars.processing-core/org.processing.core "1.5.1"]
                 [simple-open-ni "0.26.0"]]
  :dev-dependencies [[quil "1.0.0"]]
  :java-source-paths ["src/java/franklin"]
  :jar-exclusions [#"\.swp$" #"\.swo$" #"\.java"])
