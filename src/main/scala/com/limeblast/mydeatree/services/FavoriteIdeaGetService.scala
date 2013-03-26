package com.limeblast.mydeatree.services

import android.app.IntentService
import android.content.Intent
import com.limeblast.mydeatree.{FavoriteIdeaColumns, FavoriteIdea, App}
import android.util.Log

import java.util
import scala.collection.JavaConversions._


sealed class ObjectWithUri(val uri: String)


class FavoriteIdeaGetService extends IntentService("FavoriteIdeaGetService") {
  lazy val provider = App.FavoriteIdeaResource.Provider

  def onHandleIntent(intent: Intent) {
    App.FavoriteIdeaResource.getObjects() match {
      case Some(favorites) => {
        if(App.DEBUG)
          Log.d("FavoriteIdeaGetService", "Retrieved " + favorites.size() + " favorite ideas")
        // Update the database with entries from the server
        synchronizeFavoritesWithDB(favorites)
      }
      case None => if(App.DEBUG) Log.d("FavoriteIdeaGetService", "There was an error retrieving favorites.")
    }
  }

  private def synchronizeFavoritesWithDB(favorites: util.List[FavoriteIdea]) {
    val favoritesInDb = getFromDB()

    for (fav <- favorites) {
      var isNew = true
      for (obj <- favoritesInDb) {
        if (obj.uri.equals(fav.idea))
          isNew = false
      }

      if (isNew)
        insertToDB(fav)
    }

    // Remove the favorites that don't exist on server no more
    for(favInDb <- favoritesInDb) {
      var found = false

      for (fav <- favorites)
        if (fav.idea.equals(favInDb.uri))
          found = true

      if(found)
        removeFromDB(favInDb)
    }

  }

  private def getFromDB(): List[ObjectWithUri] = {
    var objectList = List[ObjectWithUri]()

    val cursor = provider.getObjects(getContentResolver, Array(FavoriteIdeaColumns.KEY_IDEA), null, null, null)

    val keyIdeaIndex = cursor.getColumnIndexOrThrow(FavoriteIdeaColumns.KEY_IDEA)

    while(cursor.moveToNext()){
      objectList = objectList :+ new ObjectWithUri(cursor.getString(keyIdeaIndex))
    }


    // Close the cursor
    cursor.close()

    objectList
  }

  private def insertToDB(fav: FavoriteIdea) =
    provider.insertObject(getContentResolver)((FavoriteIdeaColumns.KEY_ID -> fav.id),
      (FavoriteIdeaColumns.KEY_IDEA -> fav.idea),
      (FavoriteIdeaColumns.KEY_RESOURCE_URI -> fav.resource_uri))


  private def removeFromDB(fav: ObjectWithUri) =
    provider.deleteObjects(getContentResolver, (FavoriteIdeaColumns.KEY_IDEA, fav.uri), null)

}
