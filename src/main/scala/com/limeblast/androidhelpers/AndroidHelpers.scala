package com.limeblast.androidhelpers

import android.content.Context
import android.net.ConnectivityManager

object AndroidHelpers {

  def isOnline(context: Context): Boolean = {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]

    val netInfo = cm.getActiveNetworkInfo
    if (netInfo != null && netInfo.isConnectedOrConnecting)
      true
    else
      false
  }
}
