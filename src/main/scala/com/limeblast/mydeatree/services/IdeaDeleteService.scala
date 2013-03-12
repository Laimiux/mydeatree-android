package com.limeblast.mydeatree.services

import android.app.IntentService
import android.content.Intent
import com.limeblast.androidhelpers.{JsonModule, ProviderHelper}
import com.limeblast.mydeatree._
import com.limeblast.mydeatree.providers.RESTfulProvider


class IdeaDeleteService extends IntentService("IdeaDeleteService") with JsonModule {
  def onHandleIntent(intent: Intent) {
    val ideaJson = intent.getStringExtra("idea")
    if (ideaJson == null || ideaJson.equals("")) {
      throw new IllegalStateException("Idea object was not passed to delete service")
    } else {
      val idea = getMainObject(ideaJson, classOf[Idea])
      val isDeleted = App.PersonalIdeaResource.deleteIdea(idea)

      if (isDeleted) {
        removeIdea(idea)
      }
    }
  }

  /* Eventually move this function out of here */
  private def removeIdea(idea: Idea): Boolean =
    ProviderHelper.deleteObjects(getContentResolver, RESTfulProvider.CONTENT_URI, (IdeaHelper.KEY_ID, idea.id), null) match {
      case deleted: Int if (deleted > 0) => true
      case _ => false
    }

}
