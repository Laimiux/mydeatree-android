package com.limeblast.mydeatree

import android.app.{IntentService}
import android.content.{ContentUris, Intent}

import scala.Some


object IdeaUpdateService {
  val IDEA_UPDATED = 1001
  val IDEA_UPDATE_FAILED = 1002
}

class IdeaUpdateService extends IntentService("IdeaUpdateService") {
  def onHandleIntent(intent: Intent) {
    val ideaJson = intent.getStringExtra("idea")

    if (ideaJson == null || ideaJson.equals(""))
      throw new IllegalStateException("Have to pass idea to update.")


    val idea = JsonWrapper.getMainObject(ideaJson, classOf[Idea])
    MydeaTreeResourceREST.updateIdea(idea) match {
      case Some(idea) => updateIdea(idea)
      case _ =>
    }
  }

  private def updateIdea(idea: Idea) {
    val ideaAddress = ContentUris.withAppendedId(RESTfulProvider.CONTENT_URI, idea.id.toLong)
    val cr = getContentResolver
    val values = IdeaTableHelper.createNewIdeaValues(idea)
    values.put(IdeaHelper.KEY_IS_IDEA_EDITED, false)
    cr.update(ideaAddress, values, null, null)
  }
}
