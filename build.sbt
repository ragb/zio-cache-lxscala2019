import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.ruiandrebatista",
      scalaVersion := "2.12.8",
      version      := "0.1.0-SNAPSHOT",
      organizationName := "Rui Batista",
      startYear := Some(2019),
      licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))
    )),
    name := "zio-dumb-cache",

    libraryDependencies ++= Seq(
      zio,
      specs2Core % Test,
      specs2Scalacheck % Test,
      scalacheck % Test

    ),
    addCompilerPlugin("org.spire-math" % "kind-projector" % kindProjectorVersion cross CrossVersion.binary),
    git.useGitDescribe := true
  )
  .enablePlugins(GitVersioning)
