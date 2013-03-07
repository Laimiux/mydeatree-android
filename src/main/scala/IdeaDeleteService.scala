package com.limeblast.mydeatree

import android.app.IntentService
import android.content.Intent
import com.limeblast.androidhelpers.ProviderHelper


class IdeaDeleteService extends IntentService("IdeaDeleteService") {
  def onHandleIntent(intent: Intent) {
    val ideaJson = intent.getStringExtra("idea")
    if (ideaJson == null || ideaJson.equals("")) {
      throw new IllegalStateException("Idea object was not passed to delete service")
    } else {
      val idea = JsonWrapper.getMainObject(ideaJson, classOf[Idea])
      val isDeleted = MydeaTreeResourceREST.deleteIdea(idea)

      if (isDeleted) {
        removeIdea(idea)
      }
    }
  }

  /* move this function as well */
  private def removeIdea(idea: Idea): Boolean = {
    val where = IdeaHelper.KEY_ID + "=" + idea.id
    ProviderHelper.deleteObjects(getContentResolver,
      RESTfulProvider.CONTENT_URI, where, null) match {
      case deleted: Int if (deleted > 0) => true
      case _ => false
    }
  }

}
