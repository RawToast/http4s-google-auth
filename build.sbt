
name := "http4s-google-signin"

version := "0.1"

scalaVersion := "2.12.3"

mainClass in(Compile, run) := Some("Http4sServer")

enablePlugins(JavaAppPackaging, SbtTwirl)


val HTTP4S_VERSION = "0.17.0-M2"

libraryDependencies ++= http4s
libraryDependencies += "com.google.api-client" % "google-api-client" % "1.22.0"
libraryDependencies += "io.circe" %% "circe-generic" % "0.7.1"

def http4s = Seq(
  "org.http4s" %% "http4s-dsl" % HTTP4S_VERSION,
  "org.http4s" %% "http4s-blaze-server" % HTTP4S_VERSION,
  "org.http4s" %% "http4s-blaze-client" % HTTP4S_VERSION,
  "org.http4s" %% "http4s-circe" % HTTP4S_VERSION,
  "org.http4s" %% "http4s-twirl" % HTTP4S_VERSION
)
