name := "lisa-pythonic"

version := "0.0.1"

scalaVersion := "2.12.3"

cancelable in Global := true

fork in run := true

scalacOptions in ThisBuild ++= Seq(
  "-Ywarn-unused-import",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Xlint:-adapted-args"
)

libraryDependencies += "lisa-module" % "lisa-module" % "0.2.3" from "https://files.ifi.uzh.ch/seal/lisa/jar/lisa-module-assembly-0.2.3.jar"

