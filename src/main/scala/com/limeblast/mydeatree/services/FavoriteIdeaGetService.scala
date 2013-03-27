package com.limeblast.mydeatree.services

import android.app.IntentService
import android.content.Intent
import com.limeblast.mydeatree.{FavoriteIdeaProviderModule, FavoriteIdeaColumns, FavoriteIdea, App}
import android.util.Log

import java.util
import scala.collection.JavaConversions._
import com.limeblast.androidhelpers.WhereClauseModule
import android.os.ResultReceiver
import android.database.sqlite.SQLiteConstraintException


sealed class ObjectWithUri(val uri: String)


class FavoriteIdeaGetService extends IntentService("FavoriteIdeaGetService") with WhereClauseModule with FavoriteIdeaProviderModule {

  def onHandleIntent(intent: Intent) {

    val receiver = getResultReceiver(intent)

    App.FavoriteIdeaResource.getObjects() match {
      case Some(favorites) => {
        if(App.DEBUG)
          Log.d("FavoriteIdeaGetService", "Retrieved " + favorites.size() + " favorite ideas")
        // Update the database with entries from the server
        synchronizeFavoritesWithDB(favorites)

        // Send back message of success
        if (receiver != null) {
          receiver.send(0, null)
        }
      }
      case None => if(App.DEBUG) Log.d("FavoriteIdeaGetService", "There was an error retrieving favorites.")
    }
  }

  private def getResultReceiver(intent: Intent): ResultReceiver = intent.getParcelableExtra(App.FAVORITE_IDEA_GET_RESULT_RECEIVER)

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

      for (fav <- favorites if fav.idea.equals(favInDb.uri))
          found = true

      if(!found)
        removeFromDB(favInDb)
    }

  }

  private def getFromDB(): List[ObjectWithUri] = {
    var objectList = List[ObjectWithUri]()

    val select = makeWhereClause((FavoriteIdeaColumns.KEY_IS_NEW -> false))
    val cursor = getObjects(getContentResolver, Array(FavoriteIdeaColumns.KEY_IDEA), select, null, null)

    val keyIdeaIndex = cursor.getColumnIndexOrThrow(FavoriteIdeaColumns.KEY_IDEA)

    while(cursor.moveToNext()){
      objectList = objectList :+ new ObjectWithUri(cursor.getString(keyIdeaIndex))
    }


    // Close the cursor
    cursor.close()

    objectList
  }

  private def insertToDB(fav: FavoriteIdea) = {
    try {
    insertObject(getContentResolver)((FavoriteIdeaColumns.KEY_ID -> fav.id),
      (FavoriteIdeaColumns.KEY_IDEA -> fav.idea),
      (FavoriteIdeaColumns.KEY_RESOURCE_URI -> fav.resource_uri))
    } catch {
      case sql: SQLiteConstraintException => {
        if(App.DEBUG) Log.d("FavoriteIdeaGetService", "Failed to insert, so updating instead")
         updateObjects(getContentResolver,
           (FavoriteIdeaColumns.KEY_IDEA -> fav.idea),
           null,
           Map(FavoriteIdeaColumns.KEY_ID -> fav.id, FavoriteIdeaColumns.KEY_IS_NEW -> false, FavoriteIdeaColumns.KEY_IS_SYNCING -> false, FavoriteIdeaColumns.KEY_RESOURCE_URI -> fav.resource_uri))
      }
      case e: Exception => if(App.DEBUG) Log.d("FavoriteIdeaGetService", "There was an error " + e.fillInStackTrace())

    }
  }


  private def removeFromDB(fav: ObjectWithUri) =
    deleteObjects(getContentResolver, (FavoriteIdeaColumns.KEY_IDEA, fav.uri), null)

}
