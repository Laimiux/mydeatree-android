package com.limeblast.androidhelpers

import android.app.{ActivityManager, Activity}
import android.content.Context
import android.widget.Toast
import com.limeblast.mydeatree.AppSettings
import android.util.Log
import com.limeblast.mydeatree.AppSettings._

import scala.collection.JavaConversions._








trait ScalifiedActivity extends Activity with ContextModule with GetDefaultPreferencesModule with IsOnlineModule
with ScalifiedAndroid {
  self: Activity =>
  implicit val context = self


 /*
  def isServiceRunning(name: String)(implicit context: Context): Boolean = {
    context.getSystemService(Context.ACTIVITY_SERVICE) match {
      case manager: ActivityManager => {
        for(service <- manager.getRunningServices(Integer.MAX_VALUE)) {
          if(name.equals(service.service.getClassName))
            return true
        }
      }
      case _ => Log.d(APP_TAG, "isServiceRunning could not find ActivityManager")
    }

    return false
  }
  */


}

