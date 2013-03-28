package com.limeblast.mydeatree.services

import android.app.IntentService
import android.content.{ContentValues, Intent}
import com.limeblast.rest.JsonModule
import com.limeblast.mydeatree.{FavoriteIdeaProviderModule, FavoriteIdeaColumns, App, FavoriteIdea}
import android.util.Log


class FavoriteIdeaPostService extends IntentService("FavoriteIdeaPostService") with JsonModule with FavoriteIdeaProviderModule {
  def onHandleIntent(intent: Intent) {
    val favoriteIdeaJson = intent.getStringExtra("favorite_idea")

    // Make sure we have a favorite idea to upload
    if (favoriteIdeaJson == null || favoriteIdeaJson.equals(""))
      throw new IllegalStateException("You have to pass a favorite idea to this service for it to work correctly.")

    val favoriteIdea = getMainObject(favoriteIdeaJson, classOf[FavoriteIdea])


    if(App.DEBUG)
      Log.d("FavoriteIdeaPostService", "Favorite idea being posted is " + favoriteIdeaJson)
    // Update the database to mark the object as syncing
    val whereMap = Map(FavoriteIdeaColumns.KEY_IDEA -> favoriteIdea.favorite_idea)
    updateObjects(getContentResolver, whereMap, null, Map(FavoriteIdeaColumns.KEY_IS_SYNCING -> true))

    // Post the object to the server
    App.FavoriteIdeaResource.postObject(favoriteIdea) match {
      case Some(fav) => updateDatabaseEntry(fav)
      case None => {
        if(App.DEBUG)
          Log.d("FavoriteIdeaPostService", "There was an error when posting.")

        // Remove syncing part
        // Update the database to mark the object as syncing
        val whereMap = Map(FavoriteIdeaColumns.KEY_IDEA -> favoriteIdea.favorite_idea)
        updateObjects(getContentResolver, whereMap, null, Map(FavoriteIdeaColumns.KEY_IS_SYNCING -> false))
      }
    }
  }

  /**
   * Updates the database with the object that was sent back from the server
   * @param obj Object sent back
   */
  private def updateDatabaseEntry(obj: FavoriteIdea) {
    if(App.DEBUG) Log.d("FavoriteIdeaPostService", "Updating the database with " + obj.favorite_idea + " ")
    updateObjects(getContentResolver, (FavoriteIdeaColumns.KEY_IDEA -> obj.favorite_idea),
      null, Map(FavoriteIdeaColumns.KEY_ID -> obj.id,
        FavoriteIdeaColumns.KEY_IDEA -> obj.favorite_idea,
        FavoriteIdeaColumns.KEY_RESOURCE_URI -> obj.resource_uri,
        FavoriteIdeaColumns.KEY_IS_NEW -> false,
        FavoriteIdeaColumns.KEY_IS_SYNCING -> false))

  }
}
