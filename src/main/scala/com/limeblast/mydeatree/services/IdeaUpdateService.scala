package com.limeblast.mydeatree.services

import android.app.IntentService
import android.content.{ContentUris, Intent}

import com.limeblast.mydeatree._
import com.limeblast.mydeatree.providers.PrivateIdeaProvider
import scala.Some
import android.util.Log
import com.limeblast.rest.JsonModule
import storage.PrivateIdeaTableInfo


/*
object IdeaUpdateService {
  val IDEA_UPDATED = 1001
  val IDEA_UPDATE_FAILED = 1002
}

*/

class IdeaUpdateService extends IntentService("IdeaUpdateService") with JsonModule with BasicIdeaModule {
  def onHandleIntent(intent: Intent) {
    val ideaJson = intent.getStringExtra("idea")

    if (ideaJson == null || ideaJson.equals(""))
      throw new IllegalStateException("Have to pass idea to update.")

    if (App.DEBUG) Log.d("IdeaUpdateService", "Idea " + ideaJson)

    val idea = getMainObject(ideaJson, classOf[Idea])

    if(App.DEBUG) Log.d("IdeaUpdateService", "Updating idea " + idea)

    App.PersonalIdeaResource.updateObject(idea) match {
      case Some(idea) => updateIdea(idea)
      case _ =>
    }
  }

  private def updateIdea(idea: Idea) {
    if(App.DEBUG) Log.d("IdeaUpdateService", "Idea was successfully updated, reflecting the change onto the database.")

    val ideaAddress = ContentUris.withAppendedId(PrivateIdeaProvider.CONTENT_URI, idea.id.toLong)
    val cr = getContentResolver

    val values = getContentValues(idea)
    // Add personal idea specific values
    values.put(PrivateIdeaTableInfo.KEY_PUBLIC, idea.public)
    values.put(PrivateIdeaTableInfo.KEY_IS_IDEA_EDITED, false)

    cr.update(ideaAddress, values, null, null)
  }
}
