package com.limeblast.mydeatree.resources

import android.app.IntentService
import android.content.Intent
import com.limeblast.mydeatree.{App, FavoriteIdeas, AppSettings}
import android.util.Log

class FavoriteIdeaGetService extends IntentService("FavoriteIdeaGetService") {
  def onHandleIntent(intent: Intent) {
    App.FavoriteIdeaResource.getObjects() match {
      case Some(favorites) => {

      }
      case None => Log.d("FavoriteIdeaGetService", "There was an error retrieving favorites.")
    }
  }
}
