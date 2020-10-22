name := "are-you-afraid-of-the-dark"

val javacppVersion = "1.5.3"
version      := javacppVersion
scalaVersion := "2.13.1"
scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xlint")

// Platform classifier for native library dependencies
val platform = org.bytedeco.javacpp.Loader.getPlatform
// Libraries with native dependencies
val bytedecoPresetLibs = Seq(
  "javacpp" -> javacppVersion,
  "opencv" -> s"4.3.0-$javacppVersion",
  "ffmpeg" -> s"4.2.2-$javacppVersion",
  "openblas" -> s"0.3.9-$javacppVersion"
).flatMap {
  case (lib, ver) => Seq(
    // Add both: dependency and its native binaries for the current `platform`
    "org.bytedeco" % lib % ver withSources() withJavadoc(),
    "org.bytedeco" % lib % ver classifier platform
  )
}

lazy val akkaVersion = "2.6.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  "org.bytedeco" % "javacv" % javacppVersion withSources() withJavadoc(),
  "org.scala-lang.modules" %% "scala-swing" % "2.1.1",
  "junit" % "junit" % "4.13" % "test",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.novocode" % "junit-interface" % "0.11" % "test"
) ++ bytedecoPresetLibs

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += Resolver.mavenLocal

autoCompilerPlugins := true