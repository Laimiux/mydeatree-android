package com.limeblast.mydeatree.services

import android.app.IntentService
import android.content.{ContentUris, Intent}
import android.util.Log
import android.text.format.Time
import scala.collection.JavaConversions._
import java.util
import android.os.ResultReceiver
import android.net.Uri
import com.limeblast.mydeatree.AppSettings._
import com.limeblast.mydeatree._
import com.limeblast.mydeatree.AppSettings.APP_TAG
import com.limeblast.mydeatree.providers.RESTfulProvider
import scala.Some

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 2/6/13
 * Time: 11:16 PM
 * To change this template use File | Settings | File Templates.
 */
class PrivateIdeaSyncService extends IntentService("PrivateIdeaSyncService") with DatedObjectModule with BasicIdeaModule {

  override def onHandleIntent(intent: Intent) {

    val receiver: ResultReceiver = intent.getParcelableExtra(PRIVATE_IDEA_RESULT_RECEIVER)
    synchronizePrivateIdeas()


    if (receiver != null) {
      receiver.send(0, null)
    }

  }


  /**
   * Synchronizes private ideas with the server
   */
  private def synchronizePrivateIdeas() {
    if (AppSettings.DEBUG) Log.d(APP_TAG, "Synchronization started")

    // Get ideas from the server
    App.PersonalIdeaResource.getObjects match {
      case Some(ideaArray) => {
        val objectsInDb = getSavedUserIdeas()

        // Counter to see how many new ideas there are
        var newIdeas = 0
        var ideasUpdated = 0
        var ideasDeleted = 0



        /* Retrieve all private ideas from the database */
        if (ideaArray != null) {

          /* For all ideas retrieves insert or update or do nothing if old */
          for (idea <- ideaArray) {
            var isNew = true
            // Check if already in db
            for (obj <- objectsInDb) {
              // idea found in database
              if (idea.id == obj.id) {
                isNew = false
                // check if it is updated
                if (idea.modified_date != obj.modified_date) {
                  updateIdea(idea)
                  ideasUpdated += 1
                }
              }
            }

            // idea is new so insert it into the database
            if (isNew) {
              insertIdea(idea)
              newIdeas += 1
            }

          }

          // Remove ideas that don't exist on server no more
          for (obj <- objectsInDb) {
            var found = false
            for (idea <- ideaArray) {
              if (idea.id == obj.id) {
                found = true
              }
            }

            // If idea not found, remove it from db
            if (!found) {
              if (removeIdea(obj.id)) ideasDeleted += 1
            }
          }
        }

        if (AppSettings.DEBUG) Log.d(APP_TAG, "New private ideas added: " + newIdeas + ", privated ideas updated: " + ideasUpdated + ", and ideas removed " + ideasDeleted)

        val now = new Time()
        now.setToNow()
        if (AppSettings.DEBUG) Log.d(APP_TAG, "Synchronization finished at " + now.format(DATE_FORMAT))
      }
      case None => if (AppSettings.DEBUG) Log.d(APP_TAG, "There was an error syncing ideas")
    }


  }

  private def removeIdea(id: String): Boolean = {
    val where = IdeaHelper.KEY_ID + "=" + id
    val whereArgs = null

    val cr = getContentResolver
    val deletedRowCount = cr.delete(RESTfulProvider.CONTENT_URI, where, whereArgs)

    if (deletedRowCount < 1) false
    else true
  }

  private def getSavedUserIdeas(): util.ArrayList[DatedObject] = getDatedObjects(RESTfulProvider.CONTENT_URI, getContentResolver)


  /*
   * Not protected method to insert new ideas
   * watch out for already existing resources
   */
  private def insertIdea(idea: Idea) {
    val cr = getContentResolver

    //val values = IdeaTableHelper.createNewIdeaValues(idea)
    val values = getContentValues(idea)
    // Add personal idea specific values
    values.put(IdeaHelper.KEY_PUBLIC, idea.public)

    cr.insert(RESTfulProvider.CONTENT_URI, values)
  }

  private def updateIdea(idea: Idea) {
    val ideaAddress = ContentUris.withAppendedId(RESTfulProvider.CONTENT_URI, idea.id.toLong)
    val cr = getContentResolver

    val values = getContentValues(idea)
    // Add personal idea specific values
    values.put(IdeaHelper.KEY_PUBLIC, idea.public)

    cr.update(ideaAddress, values, null, null)
  }

}

/*
    private def updateIdeas() {

   val privateSyncPrefString = USERNAME + "_" + PREF_PRIVATE_SYNC
// GET LAST TIME PRIVATE IDEAS SYNCED
val context = getActivity.getApplicationContext
val prefs = PreferenceManager.getDefaultSharedPreferences(context)


LAST_PRIVATE_IDEAS_SYNCED = prefs.getString(privateSyncPrefString, null)

var url = IDEA_URL

if (LAST_PRIVATE_IDEAS_SYNCED != null) {
 // url += "?modified_date__gt=" + LAST_PRIVATE_IDEAS_SYNCED
}

/*  BAD PLACE TO SAVE
// SAVE DATE TIME WHEN LAST SYNCHRONIZED
val editor = prefs.edit()
// EVERY USER HAS HIS OWN LAST SYNC DATE
editor.putString(privateSyncPrefString, "")

val now = new Time()
now.setToNow()
editor.putString(privateSyncPrefString, now.format(DATE_FORMAT))
editor.commit()

*/

  }
*/