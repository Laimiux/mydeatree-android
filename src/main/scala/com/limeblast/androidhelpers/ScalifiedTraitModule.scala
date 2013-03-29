package com.limeblast.androidhelpers

import android.support.v4.app.Fragment
import android.content.ContentResolver
import android.app.Activity

trait ScalifiedTraitModule extends Fragment with IsOnlineModule { self: Fragment =>
  implicit val fragment = self



  def getResolver(): Option[ContentResolver] = getActivity match {
    case activity: Activity => Some(activity.getContentResolver)
    case _ => None
  }
}
