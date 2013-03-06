package com.limeblast.mydeatree

import com.actionbarsherlock.app.SherlockActivity
import com.actionbarsherlock.internal.{ActionBarSherlockCompat, ActionBarSherlockNative}

abstract class ScalaSherlockActivity extends SherlockActivity with TypedActivity {

  // proguard workaround ... Details here: https://groups.google.com/forum/?fromgroups#!topic/scala-on-android/V8pgBphHaOg
  lazy val _actionBarSherlockNativeProguardHack = new ActionBarSherlockNative(this, 0)
  lazy val _actionBarSherlockCompatProguardHack = new ActionBarSherlockCompat(this, 0)

}