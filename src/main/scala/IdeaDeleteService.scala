package com.limeblast.mydeatree

import android.app.IntentService
import android.content.Intent


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
    val whereArgs = null

    val cr = getContentResolver
    val deletedRowCount = cr.delete(RESTfulProvider.CONTENT_URI, where, whereArgs)

    if (deletedRowCount < 1) false
    else true
  }
}
