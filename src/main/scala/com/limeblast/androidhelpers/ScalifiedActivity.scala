package com.limeblast.androidhelpers

import android.app.Activity
import android.content.Context
import android.widget.Toast
import android.net.ConnectivityManager


sealed trait ShowToastModuleWithImplicitContext{
  def shortToast[T <: Context](msg: String)(implicit context: T) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

  def longToast[T <: Context](msg: String)(implicit context: T) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

sealed trait ContextActivityModule extends IntentConversions {

  def startActivity[T <: Activity](activityClass: Class[T])(implicit context: Context) = context.startActivity(activityClass)

  def startActivityForResult[T <: Activity](implicit context: Activity, activityClass: Class[T], requestCode: Int): Unit = context.startActivityForResult(activityClass, requestCode)

}



trait ScalifiedActivity extends Activity with ShowToastModuleWithImplicitContext
with ContextActivityModule with GetDefaultPreferencesModule with IsOnlineModule {
  self: Activity =>
  implicit val context = self


}

