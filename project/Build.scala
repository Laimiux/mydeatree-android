import sbt._

import Keys._
import AndroidKeys._

object General {
  val settings = Defaults.defaultSettings ++ Seq (
    name := "MydeaTree",
    version := "0.1",
    versionCode := 0,
    scalaVersion := "2.9.2",
    platformName in Android := "android-14"
  )

  val pgOptions = Seq("-keep class android.support.v4.app.** { *; }",
    "-keep interface android.support.v4.app.** { *; }",
    "-keep class com.actionbarsherlock.** { *; }",
    "-keep interface com.actionbarsherlock.** { *; }",
    "-keepattributes *Annotation*").mkString(" ")

  val proguardSettings = Seq (
    useProguard in Android := true,
    proguardOption in Android := pgOptions
  )

  lazy val fullAndroidSettings =
    General.settings ++
    AndroidProject.androidSettings ++
    TypedResources.settings ++
    proguardSettings ++
    AndroidManifestGenerator.settings ++
    AndroidMarketPublish.settings ++ Seq (
      keyalias in Android := "change-me",
      libraryDependencies += "org.scalatest" %% "scalatest" % "1.8" % "test",
      libraryDependencies ++= Seq("com.actionbarsherlock" % "actionbarsherlock" % "4.2.0" artifacts(Artifact("actionbarsherlock", "apklib", "apklib")) from
        "https://oss.sonatype.org/content/groups/scala-tools/",
        "com.google.android" % "support-v4" % "r7")
    )
}

object AndroidBuild extends Build {
  lazy val main = Project (
    "MydeaTree",
    file("."),
    settings = General.fullAndroidSettings
  )

  lazy val tests = Project (
    "tests",
    file("tests"),
    settings = General.settings ++
               AndroidTest.androidSettings ++
               General.proguardSettings ++ Seq (
      name := "MydeaTreeTests"
    )
  ) dependsOn main
}
