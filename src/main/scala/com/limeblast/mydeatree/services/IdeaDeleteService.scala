package com.limeblast.mydeatree.services

import android.app.IntentService
import android.content.Intent
import com.limeblast.androidhelpers.{ProviderAccessModule}
import com.limeblast.mydeatree._
import com.limeblast.mydeatree.providers.PrivateIdeaProvider
import android.util.Log
import com.limeblast.rest.JsonModule
import storage.PrivateIdeaTableInfo


class IdeaDeleteService extends IntentService("IdeaDeleteService") with JsonModule with ProviderAccessModule {
  def onHandleIntent(intent: Intent) {


    val ideaJson = intent.getStringExtra("idea")
    if (ideaJson == null || ideaJson.equals("")) {
      throw new IllegalStateException("Idea object was not passed to delete service")
    } else {
      val idea = getMainObject(ideaJson, classOf[Idea])

      Log.d("IdeaDeleteService", "Deleting idea " + idea.resource_uri)

      //val isDeleted = App.PersonalIdeaResource.deleteIdea(idea)

      val isDeleted = App.PersonalIdeaResource.deleteObject(idea)
      Log.d("IdeaDeleteService", "Idea was deleted " + isDeleted)

      if (isDeleted) removeIdea(idea)
      else Log.d("IdeaDeleteService", "There was an error when deleting an idea")

    }
  }

  private def removeIdea(idea:Idea) {

    ProviderHelper.deleteObjects(getContentResolver, PrivateIdeaProvider.CONTENT_URI, (PrivateIdeaTableInfo.KEY_ID, idea.id), null)
  }

  /*
  /* Eventually move this function out of here */
  private def removeIdea(idea: Idea): Boolean = {
  /*  Get this to work at some point
    App.IdeaProvider.deleteObjects(getContentResolver, (PrivateIdeaTableInfo.KEY_ID, idea.id), null) match {
      case deleted:Int if (deleted > 0) => true
      case _ => false
    }
     */
    val deleted = ProviderHelper.deleteObjects(getContentResolver, PrivateIdeaProvider.CONTENT_URI, (PrivateIdeaTableInfo.KEY_ID, idea.id), null)
    if (deleted > 0) true
    else false
  }
  */


}
