import org.typelevel.scalacoptions.ScalacOptions

lazy val versions = new {
  val scalaTest = "3.2.11"
  val scalaTestScalaCheck = "3.2.11.0"
  val cats = "2.10.0"
  val catsEffect = "3.5.1"
  val catsMtl = "1.3.1"
  val sourcecode = "0.3.1"
  val monix = "3.4.1"
  val magnoliaScala2 = "0.17.0"
  val magnoliaScala3 = "2.0.0-M4"
  val scalaCheck = "1.15.4"
  val zio = "1.0.9"
  val zioCats = "3.1.1.0"
  val slf4j = "1.7.36"
  val log4j = "2.20.0"
  val disruptor = "3.4.4"
  val scribe = "3.12.2"
  val perfolation = "1.2.9"
  val jsoniter = "2.23.4"
}

lazy val scalaVersions = List("2.13.12", "2.12.18", "3.3.1")

lazy val scalaTest = "org.scalatest" %% "scalatest" % versions.scalaTest % Test
lazy val scalaTestScalaCheck = "org.scalatestplus" %% "scalacheck-1-15" % versions.scalaTestScalaCheck % Test

lazy val alleycats = "org.typelevel" %% "alleycats-core" % versions.cats

lazy val cats = List(
  (version: String) => "org.typelevel" %% "cats-core" % version,
  (version: String) => "org.typelevel" %% "cats-laws" % version % Test
).map(_.apply(versions.cats))

lazy val catsEffect = "org.typelevel" %% "cats-effect" % versions.catsEffect
lazy val catsEffectStd = "org.typelevel" %% "cats-effect-std" % versions.catsEffect

lazy val catsMtl = "org.typelevel" %% "cats-mtl" % versions.catsMtl

lazy val sourcecode = "com.lihaoyi" %% "sourcecode" % versions.sourcecode

lazy val monixCatnap = "io.monix" %% "monix-catnap" % versions.monix

lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % versions.scalaCheck % Test

lazy val monix = "io.monix" %% "monix" % versions.monix

lazy val magnoliaScala2 = "com.propensive" %% "magnolia" % versions.magnoliaScala2
lazy val magnoliaScala3 = "com.softwaremill.magnolia" %% "magnolia-core" % versions.magnoliaScala3

lazy val perfolation = "com.outr" %% "perfolation" % versions.perfolation

lazy val slf4j = "org.slf4j" % "slf4j-api" % versions.slf4j

lazy val log4j = ("com.lmax" % "disruptor" % versions.disruptor) :: List(
  "org.apache.logging.log4j" % "log4j-api",
  "org.apache.logging.log4j" % "log4j-core"
).map(_ % versions.log4j)

lazy val scribe = List(
  "com.outr" %% "scribe" % versions.scribe,
  "com.outr" %% "scribe-file" % versions.scribe
)

lazy val jsoniter = List(
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % versions.jsoniter,
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % versions.jsoniter % "compile-internal"
)

lazy val noPublish = Seq(
  publish / skip := true
)

lazy val sharedSettings = Seq(
  scalaVersion := scalaVersions.head,
  organization := "com.github.valskalla",
  libraryDependencies ++= scalaTestScalaCheck :: scalaCheck :: scalaTest :: Nil,
  crossScalaVersions := scalaVersions,
  classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.ScalaLibrary,
  homepage := Some(url("https://github.com/valskalla/odin")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "sergeykolbasov",
      "Sergey Kolbasov",
      "whoisliar@gmail.com",
      url("https://github.com/sergeykolbasov")
    ),
    Developer(
      "Doikor",
      "Aki Huttunen",
      "doikor@gmail.com",
      url("https://github.com/Doikor")
    )
  ),
  libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, _)) =>
      List(
        compilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full),
        compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
      )
    case _ => Nil
  }),
  // Avoid compiler warnings due to scalatest DSL
  Test / tpolecatExcludeOptions += ScalacOptions.warnNonUnitStatement
)

lazy val `odin-core` = (project in file("core"))
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= (catsEffect % Test) :: catsMtl :: sourcecode :: perfolation :: catsEffectStd :: alleycats :: cats
  )

lazy val `odin-json` = (project in file("json"))
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= jsoniter
  )
  .dependsOn(`odin-core` % "compile->compile;test->test")

lazy val `odin-zio` = (project in file("zio"))
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      catsEffect,
      "dev.zio" %% "zio" % versions.zio,
      "dev.zio" %% "zio-interop-cats" % versions.zioCats
    )
  )
  .dependsOn(`odin-core` % "compile->compile;test->test")

lazy val `odin-monix` = (project in file("monix"))
  .settings(sharedSettings)
  .settings(
    libraryDependencies += monix
  )
  .dependsOn(`odin-core` % "compile->compile;test->test")

lazy val `odin-slf4j` = (project in file("slf4j"))
  .settings(sharedSettings)
  .settings(
    libraryDependencies += slf4j
  )
  .dependsOn(`odin-core` % "compile->compile;test->test")

lazy val `odin-extras` = (project in file("extras"))
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) => List(magnoliaScala3)
      case _ =>
        List(
          magnoliaScala2,
          // only in provided scope so that users of extras not relying on magnolia don't get it on their classpaths
          // see extras section In Readme
          "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
        )
    })
  )
  .dependsOn(`odin-core` % "compile->compile;test->test")

lazy val benchmarks = (project in file("benchmarks"))
  .settings(sharedSettings)
  .settings(noPublish)
  .enablePlugins(JmhPlugin)
  .settings(
    libraryDependencies ++= catsEffect :: scribe ::: log4j
  )
  .dependsOn(`odin-core`, `odin-json`)

lazy val docs = (project in file("odin-docs"))
  .settings(sharedSettings)
  .settings(noPublish)
  .settings(
    mdocVariables := Map(
      "VERSION" -> version.value
    ),
    mdocOut := file("."),
    libraryDependencies += catsEffect,
    tpolecatExcludeOptions += ScalacOptions.warnNonUnitStatement
  )
  .dependsOn(`odin-core`, `odin-json`, `odin-zio`, /*`odin-monix`,*/ `odin-slf4j`, `odin-extras`)
  .enablePlugins(MdocPlugin)

lazy val examples = (project in file("examples"))
  .settings(sharedSettings)
  .settings(
    coverageExcludedPackages := "io.odin.examples.*",
    libraryDependencies += catsEffect
  )
  .settings(noPublish)
  .dependsOn(`odin-core` % "compile->compile;test->test", `odin-zio`)

lazy val odin = (project in file("."))
  .settings(sharedSettings)
  .settings(noPublish)
  .dependsOn(`odin-core`, `odin-json`, `odin-zio`, /* `odin-monix`,*/ `odin-slf4j`, `odin-extras`)
  .aggregate(`odin-core`, `odin-json`, `odin-zio`, /* `odin-monix`,*/ `odin-slf4j`, `odin-extras`, benchmarks, examples)
