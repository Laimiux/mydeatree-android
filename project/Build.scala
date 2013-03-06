import sbt._

import Keys._
import AndroidKeys._

object General {
  val settings = Defaults.defaultSettings ++ Seq(
    name := "MydeaTree",
    version := "0.1",
    versionCode := 0,
    scalaVersion := "2.10.0",
    platformName in Android := "android-14",
    compileOrder := CompileOrder.JavaThenScala,
    javacOptions += "-g:none",
    javacOptions ++= Seq("-source", "1.6"),
    resolvers += "Maven Search" at "http://repo1.maven.org/maven2/",
    resolvers += "Sonatype" at "https://oss.sonatype.org/content/groups/scala-tools/"

  )

  //

  val pgOptions = Seq("-keep class android.support.v4.app.** { *; }",
    "-keep interface android.support.v4.app.** { *; }",
    "-keep class com.actionbarsherlock.** { *; }",
    "-keep interface com.actionbarsherlock.** { *; }",
    "-keepattributes *Annotation*").mkString("")

  val proguardSettings = Seq(
    useProguard in Android := true,
    proguardOption in Android := pgOptions
  )

  lazy val fullAndroidSettings =
    General.settings ++
      AndroidProject.androidSettings ++
      TypedResources.settings ++
      proguardSettings ++
      AndroidManifestGenerator.settings ++
      AndroidMarketPublish.settings ++ Seq(
      keyalias in Android := "mydeatree",
      libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test",
      libraryDependencies += "com.google.android" % "support-v4" % "r7",
      //libraryDependencies += "com.actionbarsherlock" % "actionbarsherlock" % "4.2.0" artifacts(Artifact("actionbarsherlock", "apklib", "apklib")) from "https://oss.sonatype.org/content/groups/scala-tools/",
      //    libraryDependencies += "com.actionbarsherlock" % "actionbarsherlock" % "4.2.0"  artifacts(Artifact("actionbarsherlock-4.2.0", "apklib", "apklib")) from "http://repo1.maven.org/maven2/",
      //artifacts(Artifact("actionbarsherlock", "apklib", "apklib")) from "https://oss.sonatype.org/content/groups/scala-tools/",
      libraryDependencies += "com.j256.ormlite" % "ormlite-android" % "4.43",
      libraryDependencies += "org.scalaz" % "scalaz_2.10" % "6.0.4",
      libraryDependencies += "com.actionbarsherlock" % "actionbarsherlock" % "4.2.0" artifacts (Artifact("actionbarsherlock", "apklib", "apklib"))
      //libraryDependencies += "com.actionbarsherlock" % "library" % "4.0.2"  artifacts(Artifact("library", "apklib", "apklib")),

    )
}

object AndroidBuild extends Build {
  lazy val actionbarsherlock = Project(
    "actionbarsherlock",
    file("actionbarsherlock"),
    settings = General.fullAndroidSettings ++ Seq(
      name := "ActionBarSherlock"
    )
    /*
      settings = General.settings ++
        AndroidTest.androidSettings ++
        General.proguardSettings ++ Seq (
        name := "ActionBarSherlock"
      )
      */
  )

  lazy val main = Project(
    "MydeaTree",
    file("."),
    settings = General.fullAndroidSettings
  ) //dependsOn actionbarsherlock

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
