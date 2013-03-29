package com.limeblast.androidhelpers

import android.content.Context
import android.net.ConnectivityManager

/**
 * Module that can check if there
 * is network connection
 */
trait IsOnlineModule {
  def isOnline(implicit context: Context): Boolean = {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]

    val netInfo = cm.getActiveNetworkInfo
    if (netInfo != null && netInfo.isConnectedOrConnecting)
      true
    else
      false
  }
}
