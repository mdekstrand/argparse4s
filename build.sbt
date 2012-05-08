name := "argparse4s"

organization := "net.elehack"

version := "0.1"

scalaVersion := "2.9.2"

crossScalaVersions := Seq("2.9.2", "2.9.1", "2.9.1-1", "2.9.0-1")

libraryDependencies ++= Seq(
  "net.sourceforge.argparse4j" % "argparse4j" % "0.2.1",
  "org.scalatest" %% "scalatest" % "1.7.2" % "test"
)
