package com.limeblast.androidhelpers

import android.app.{ActivityManager, Activity}
import android.content.Context
import android.support.v4.app.Fragment
import android.util.Log
import com.limeblast.mydeatree.AppSettings._

import scala.collection.JavaConversions._


trait BaseContextModule {
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
}

trait ContextModule extends IntentConversions with BaseContextModule {

 def startActivity[T <: Activity](activityClass: Class[T])(implicit context: Context) =
   context.startActivity(activityClass)

 def startActivityForResult[T <: Activity](implicit context: Activity, activityClass: Class[T], requestCode: Int): Unit =
   context.startActivityForResult(activityClass, requestCode)

}


trait ContextTraitModule extends Fragment with IntentConversions with BaseContextModule {

  def startActivity[T <: Activity](activityClass: Class[T]): Unit = startActivity(activityClass)
}
