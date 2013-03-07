package com.limeblast.androidhelpers

import android.app.IntentService
import android.content.Intent

class QuickIntentService[F](val name: String, f: Intent => F) extends IntentService(name) {
  def onHandleIntent(intent: Intent)  = f(intent)
}
