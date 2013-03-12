package com.limeblast.mydeatree.resources

import android.app.IntentService
import android.content.Intent
import com.limeblast.mydeatree.{FavoriteIdeas, AppSettings}
import android.util.Log

class FavoriteIdeaGetService extends IntentService("FavoriteIdeaGetService") {
  def onHandleIntent(intent: Intent) {

    /*RESTCalls.retrieveObjects(AppSettings.FAVORITE_IDEAS_URL, classOf[FavoriteIdeas]) match {
        case Some(ideas) => {

        }
        case None => Log.d("FAVORITE_IDEA_GET_SERVICE", "There was an error retrieving ideas")
      }
      */
  }
}
