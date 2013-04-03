package com.limeblast.mydeatree

import android.content.{Context, Intent}
import services.PrivateIdeaSyncService

/**
 * Module that allows you to sync all personal
 * resources
 */
trait PersonalResourceSync {
 implicit def context:Context

 def startSyncingAllPersonalResources() = {
   startPersonalIdeaSync(implicitly[Context])
 }

 def startPersonalIdeaSync(implicit context: Context) = {
   val intent = new Intent(context, classOf[PrivateIdeaSyncService])
   context.startService(intent)
 }
}
