name := "code-state-machine-scala"

version := "1.0"

scalaVersion := "2.11.7"

sbtVersion := "0.13.9"

seq(bintraySettings: _*)

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "org.slf4j" % "slf4j-log4j12" % "1.7.7" % "test",
  "org.clapper" %% "grizzled-slf4j" % "1.0.2"
)

scalacOptions in(Compile, doc) ++= Seq("-doc-root-content", baseDirectory.value + "/root-doc.txt")

logLevel := Level.Debug