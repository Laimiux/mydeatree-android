import sbt._

import Keys._
import AndroidKeys._

object General {
  val settings = Defaults.defaultSettings ++ Seq(
    name := "MydeaTree",
    organization := "com.limeblast",
    version := "0.1",
    versionCode := 0,
    scalaVersion := "2.10.1",
    platformName in Android := "android-14",
    compileOrder := CompileOrder.JavaThenScala,
    javacOptions += "-g:none",
    //javacOptions ++= Seq("-source", "1.6"),
    resolvers += "Maven Search" at "http://repo1.maven.org/maven2/",
    resolvers += "Sonatype" at "https://oss.sonatype.org/content/groups/scala-tools/",
    resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"
  )

  //

  val pgOptions = Seq("-keep class android.support.v4.app.** { *; }",
    "-keep interface android.support.v4.app.** { *; }",
    "-keep class com.actionbarsherlock.** { *; }",
    "-keep class com.limeblast.** { *; }",
    "-keep class com.limeblast.mydeatree.activities.** { *; }",
    "-keep interface com.actionbarsherlock.** { *; }",
    "-keep class scala.collection.SeqLike { public protected *; }",
    "-keepattributes *Annotation*").mkString("")

  val proguardSettings = Seq(
    useProguard in Android := true,
    proguardOption in Android := pgOptions
  )

  // Base settings for other projects
  lazy val baseSettings =
    General.settings ++
      AndroidProject.androidSettings ++
      TypedResources.settings ++
      proguardSettings ++
      AndroidManifestGenerator.settings


  /*
  lazy val androidSettings = General.baseSettings ++
    AndroidManifestGenerator.settings ++
    AndroidMarketPublish.settings ++ Seq(
    keyalias in Android := "mydeatree",
    libraryDependencies += "com.google.android" % "support-v4" % "r7",
    libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test",
    //libraryDependencies += "com.actionbarsherlock" % "actionbarsherlock" % "4.2.0" artifacts(Artifact("actionbarsherlock", "apklib", "apklib")) from "https://oss.sonatype.org/content/groups/scala-tools/",
    //    libraryDependencies += "com.actionbarsherlock" % "actionbarsherlock" % "4.2.0"  artifacts(Artifact("actionbarsherlock-4.2.0", "apklib", "apklib")) from "http://repo1.maven.org/maven2/",
    //artifacts(Artifact("actionbarsherlock", "apklib", "apklib")) from "https://oss.sonatype.org/content/groups/scala-tools/",
    libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0.4",
    libraryDependencies += "com.actionbarsherlock" % "actionbarsherlock" % "4.2.0" artifacts (Artifact("actionbarsherlock", "apklib", "apklib"))

  )
  */


  //lazy val fullAndroidSettings = ???


  // Settings for the main app.
  lazy val mainSettings =
    General.baseSettings ++
      AndroidMarketPublish.settings ++ Seq(
      keyalias in Android := "mydeatree",
      libraryDependencies += "com.google.android" % "support-v4" % "r7",
      libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test",
      //libraryDependencies += "com.actionbarsherlock" % "actionbarsherlock" % "4.2.0" artifacts(Artifact("actionbarsherlock", "apklib", "apklib")) from "https://oss.sonatype.org/content/groups/scala-tools/",
      //    libraryDependencies += "com.actionbarsherlock" % "actionbarsherlock" % "4.2.0"  artifacts(Artifact("actionbarsherlock-4.2.0", "apklib", "apklib")) from "http://repo1.maven.org/maven2/",
      //artifacts(Artifact("actionbarsherlock", "apklib", "apklib")) from "https://oss.sonatype.org/content/groups/scala-tools/",
      libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0.4",
      libraryDependencies += "com.actionbarsherlock" % "actionbarsherlock" % "4.2.0" artifacts (Artifact("actionbarsherlock", "apklib", "apklib"))

    )
}

object AndroidBuild extends Build {
  lazy val main = Project(
    "MydeaTree",
    file("."),
    settings = General.mainSettings
  ) dependsOn macros

  lazy val macros = Project(
    "androidmacros",
    file("androidmacros"),
    settings = General.baseSettings ++ Seq(
      name := "AndroidMacros",
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-compiler" % _)
    )
  )

  lazy val tests = Project(
    "tests",
    file("tests"),
    settings = General.settings ++ AndroidTest.androidSettings ++ Seq(
      name := "MydeaTreeTests", // You need to name your test project differently to avoid a circular dependency error
      libraryDependencies += "com.jayway.android.robotium" % "robotium-solo" % "2.4", // If you're using Robotium
      libraryDependencies += "junit" % "junit" % "3.8.2" // If you're using Junit
    )
  ) dependsOn main

}
