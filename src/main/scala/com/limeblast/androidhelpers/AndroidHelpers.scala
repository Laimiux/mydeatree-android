package com.limeblast.androidhelpers

import android.content.{Intent, Context}
import android.net.ConnectivityManager
import android.app.Activity

object AndroidHelpers {

  def isOnline(context: Context): Boolean = {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]

    val netInfo = cm.getActiveNetworkInfo
    if (netInfo != null && netInfo.isConnectedOrConnecting)
      true
    else
      false
  }

  def changeActivity[T <: Activity](ctx: Activity, activity: Class[T]) {
    val intent = new Intent()
    intent.setClass(ctx, activity)
    ctx.startActivity(intent)
    ctx.finish()
  }
}
