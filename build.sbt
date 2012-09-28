name := "argparse4s"

organization := "net.elehack.argparse4s"

organizationName := "Michael Ekstrand"

version := "0.3-SNAPSHOT"

scalaVersion := "2.9.2"

crossScalaVersions := Seq("2.9.2", "2.9.1", "2.9.1-1", "2.9.0-1")

compileOrder := CompileOrder.JavaThenScala

libraryDependencies ++= Seq(
  "net.sourceforge.argparse4j" % "argparse4j" % "0.2.1",
  "org.scalatest" %% "scalatest" % "1.7.2" % "test"
)

publishTo <<= {
  val nexus = "https://oss.sonatype.org/"
  val Snapshot = "\\s*(.*)-SNAPSHOT\\s*$".r
  version {
    case Snapshot(_) =>
      Some("snapshots" at nexus + "content/repositories/snapshots")
    case _ =>
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }
}

pomExtra := (
  <url>http://bitbucket.org/elehack/argparse4s</url>
  <licenses>
    <license>
      <name>MIT</name>
      <url>http://www.opensource.org/licenses/mit-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>http://bitbucket.org/elehack/argparse4s</url>
    <connection>scm:hg:https://bitbucket.org/elehack/argparse4s</connection>
  </scm>
  <developers>
    <developer>
      <id>elehack</id>
      <name>Michael Ekstrand</name>
      <url>http://elehack.net/michael/</url>
    </developer>
  </developers>
)
