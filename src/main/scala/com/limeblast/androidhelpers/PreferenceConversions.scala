package com.limeblast.androidhelpers

import android.app.Activity
import android.content.{Context, SharedPreferences}
import android.preference.PreferenceManager


trait GetDefaultPreferencesModule {
  def getDefaultPreferences()(implicit context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
}

trait EasyAccessOfPreferencesModule[T <: Activity] {
  implicit def act: T

  def getDefaultPreferences(): Option[SharedPreferences] = act match {
    case a:Activity => Some(PreferenceManager.getDefaultSharedPreferences(a.getApplicationContext))
    case null => None
  }
}

trait PreferenceConversions {
  implicit def activityCanAccessDefaultPreferences[T <: Activity](activity: T): EasyAccessOfPreferencesModule[T] = new EasyAccessOfPreferencesModule[T] {
    val act: T = activity
  }


}
