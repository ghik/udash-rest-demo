
/* The IntelliJ hack start */
val forIdeaImport = System.getProperty("idea.managed", "false").toBoolean

def mkSourceDirs(base: File, scalaBinary: String, conf: String): Seq[File] = Seq(
  base / "src" / conf / "scala",
  base / "src" / conf / s"scala-$scalaBinary",
  base / "src" / conf / "java"
)

def mkResourceDir(base: File, conf: String): File =
  base / "src" / conf / "resources"

def sourceDirsSettings(baseMapper: File => File) = Seq(
  unmanagedSourceDirectories in Compile ++=
    mkSourceDirs(baseMapper(baseDirectory.value), scalaBinaryVersion.value, "main"),
  unmanagedSourceDirectories in Test ++=
    mkSourceDirs(baseMapper(baseDirectory.value), scalaBinaryVersion.value, "test"),
  unmanagedResourceDirectories in Compile +=
    mkResourceDir(baseMapper(baseDirectory.value), "main"),
  unmanagedResourceDirectories in Test +=
    mkResourceDir(baseMapper(baseDirectory.value), "test")
)

def sameNameAs(proj: Project) =
  if (forIdeaImport) Seq.empty
  else Seq(name := (name in proj).value)
/* The IntelliJ hack end */

val silencerVersion = "1.6.0"
val scalatestVersion = "3.1.1"
val scalajsDomVersion = "1.0.0"
val udashVersion = "0.8.3"
val jettyVersion = "9.4.28.v20200408"

val commonSettings = Seq(
  scalaVersion := "2.12.11",

  scalacOptions ++= Seq(
    "-encoding", "utf-8",
    "-Yrangepos",
    "-explaintypes",
    "-feature",
    "-deprecation",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint:-missing-interpolator,-adapted-args,-unused,_",
  ),

  // force IntelliJ and SBT to compile into separate directories to avoid conflicts
  ideOutputDirectory in Compile := Some(target.value.getParentFile / "out/production"),
  ideOutputDirectory in Test := Some(target.value.getParentFile / "out/test"),

  // universal dependencies
  libraryDependencies ++= Seq(
    // for warning suppression
    compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full,
    // the most popular test framework
    "org.scalatest" %%% "scalatest" % scalatestVersion % Test,
  ),
)

// `Def.setting` is used because %%% evaluates to different things in JVM projects and JS projects
val coreCrossDeps = Def.setting(Seq(
  // %%% because this is a library cross compiled for JVM, JS and different Scala & Scala.js versions
  // The actual artifact name is `udash-rest_2.12` for JVM and `udash-rest_2.12_sjs0.6` for JS
  "io.udash" %%% "udash-rest" % udashVersion,
))
val coreJvmDeps = Seq(
  // Single % because this is a pure Java dependency, not cross compiled
  "org.eclipse.jetty" % "jetty-server" % jettyVersion,
  "org.eclipse.jetty" % "jetty-servlet" % jettyVersion,
)
val coreJsDeps = Def.setting(Seq(
  "org.scala-js" %%% "scalajs-dom" % scalajsDomVersion,
))

// root project which has no sources and only aggregates other modules
// "aggregating" means that when you run some SBT task for root project, it is being run for all aggregated projects
// for example: running 'compile' from SBT will trigger 'core/compile' and 'core-js/compile'
// NOTE: sbt "project" == IntelliJ "module"

lazy val urdemo = project.in(file("."))
  .aggregate(core, `core-js`)

// core/src     - cross compiled sources
// core/jvm/src - JVM-only sources
// core/js/src  - JS-only sources
//
// JVM-only sources and JS-only sources can use things from cross-compiled sources
// cross compiled sources can also use things from JVM-only and JS-only sources as long as these things are
// implemented for *both* JVM and JS (e.g. the same class implemented differently for JVM and JS)

lazy val core = project
  .settings(
    commonSettings,
    sourceDirsSettings(_ / "jvm"), // adding JVM-only sources
    libraryDependencies ++= coreCrossDeps.value,
    libraryDependencies ++= coreJvmDeps
  )

lazy val `core-js` = project.in(core.base / "js")
  .enablePlugins(ScalaJSPlugin)
  .configure(p => if (forIdeaImport) p.dependsOn(core) else p)
  .settings(
    commonSettings,
    sameNameAs(core),
    sourceDirsSettings(_.getParentFile), // adding cross-compiled sources from the `core` project
    libraryDependencies ++= coreCrossDeps.value,
    libraryDependencies ++= coreJsDeps.value
  )
