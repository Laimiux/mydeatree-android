package com.limeblast.mydeatree

import activities.MainActivity
import android.content.{Context, Intent}
import services.PrivateIdeaSyncService
import com.limeblast.mydeatree.AppSettings._
import android.os.Bundle

/**
 * Module that allows you to sync all personal
 * resources
 */
trait PersonalResourceSync {
 def context:Context

 def startSyncingAllPersonalResources() = {
   startPersonalIdeaSync()
 }

 def startPersonalIdeaSync() = {
   val intent = new Intent(context, classOf[PrivateIdeaSyncService])
   context.startService(intent)
 }
}
