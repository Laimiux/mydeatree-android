package com.limeblast.androidhelpers

import android.support.v4.app.Fragment
import android.content.{ComponentName, Intent, ContentResolver}
import android.app.Activity

trait ScalifiedTraitModule extends Fragment with ContextTraitModule
with ScalifiedAndroid with IsOnlineModule { self: Fragment =>
  implicit val fragment = self

  def getResolver(): Option[ContentResolver] = getActivity match {
    case activity: Activity => Some(activity.getContentResolver)
    case _ => None
  }


  def startService(intent: Intent): Option[ComponentName] = {
    getActivity match {
      case activity: Activity => Some(activity.startService(intent))
      case _ => None
    }
  }



}
