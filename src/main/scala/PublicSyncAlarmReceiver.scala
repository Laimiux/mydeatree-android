package com.limeblast.mydeatree

import android.content.{Intent, Context, BroadcastReceiver}
import android.util.Log
import services.PublicIdeaSyncService

object PublicSyncAlarmReceiver {
  val ACTION_REFRESH_PUBLIC_IDEAS_ALARM = "com.limeblast.mydeatree.ACTION_REFRESH_PUBLIC_IDEAS_ALARM"
  val APP_TAG = "PUBLIC_SYNC_ALARM_RECEIVER"
}

class PublicSyncAlarmReceiver extends BroadcastReceiver {

  override def onReceive(context: Context, intent: Intent) {
    if(AppSettings.DEBUG) Log.d(PublicSyncAlarmReceiver.APP_TAG, "Broadcast received")
    val startIntent = new Intent(context, classOf[PublicIdeaSyncService])
    context.startService(startIntent)
  }

}
