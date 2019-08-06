import sbt._

object Dependencies{

  lazy val zio = "dev.zio" %% "zio" % zioVersion
  lazy val specs2Core = "org.specs2" %% "specs2-core" % specs2Version
  lazy val specs2Scalacheck = "org.specs2" %% "specs2-scalacheck" % specs2Version
  lazy val scalacheck =  "org.scalacheck" %% "scalacheck" % scalacheckVersion

  val zioVersion = "1.0.0-RC9"
  val specs2Version = "4.5.1"
  val scalacheckVersion = "1.14.0"
  val kindProjectorVersion = "0.9.7"
}
