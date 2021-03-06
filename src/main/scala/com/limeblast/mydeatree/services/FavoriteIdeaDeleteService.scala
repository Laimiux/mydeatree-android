package com.limeblast.mydeatree.services

import android.app.IntentService
import android.content.Intent
import com.limeblast.rest.JsonModule
import com.limeblast.mydeatree.{FavoriteIdeaProviderModule, App, FavoriteIdea}
import android.util.Log
import com.limeblast.mydeatree.storage.FavoriteIdeaColumns

class FavoriteIdeaDeleteService extends IntentService("FavoriteIdeaDeleteService") with JsonModule with FavoriteIdeaProviderModule{
  def onHandleIntent(intent: Intent) {
    val favoriteIdeaJson = intent.getStringExtra("favorite_idea")

    if (favoriteIdeaJson == null || favoriteIdeaJson.equals(""))
      throw new IllegalStateException("Needs a favorite_idea object to passed in the intent")

    val favorite = getMainObject(favoriteIdeaJson, classOf[FavoriteIdea])

    val isDeleted = App.FavoriteIdeaResource.deleteObject(favorite)


    if(isDeleted)
      removeFromDB(favorite)
    else if(App.DEBUG)
      Log.d("FavoriteIdeaDeleteService", "There was issue when deleting...")
  }

  private def removeFromDB(fav: FavoriteIdea) =
    deleteObjects(getContentResolver, (FavoriteIdeaColumns.KEY_IDEA -> fav.favorite_idea), null)

}
