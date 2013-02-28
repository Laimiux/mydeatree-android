package com.limeblast.mydeatree

import java.util
import android.util.Log

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 1/10/13
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */
object MydeaTreeResourceREST {
  private val APP_TAG = "IDEA_RESOURCE_REST"

  def retrieveIdeas(initialUrl: String): util.ArrayList[Idea] = {
    val objects = RESTCalls.retrieveObjects(initialUrl, classOf[PersonalIdeas])
    if (objects != null) {
      if(AppSettings.DEBUG) Log.d(APP_TAG, "Retrieved " + objects.size() + " ideas")
      return objects.asInstanceOf[util.ArrayList[Idea]]
    }
    else {
      return null
    }
  }

  def retrievePublicIdeas(initialUrl: String): util.ArrayList[PublicIdea] = {
    val objects = RESTCalls.retrieveObjects(initialUrl, classOf[PublicIdeas])
    if (objects != null)
      objects.asInstanceOf[util.ArrayList[PublicIdea]]
    else
      null
  }

  def getIdea(url: String): Idea = {
    RESTCalls.retrieveObject(url, classOf[Idea]) match {
      case Some(x) => return x
      case None => return null
    }
  }

  def postIdea(url: String, idea: Idea): Option[Idea] = {
    val finalIdea: Option[Idea] = RESTCalls.postObject(url, idea)

    finalIdea
  }

  def updateIdea(newIdea: Idea): Option[Idea] = {
    val url = AppSettings.API_URL + newIdea.resource_uri

    val updatedIdea = RESTCalls.putObject(url, newIdea)

    updatedIdea
  }

  def deleteIdea(idea: Idea): Boolean = {
    if(AppSettings.DEBUG) Log.d(APP_TAG, "Deleting an idea")
    return RESTCalls.deleteObject(AppSettings.API_URL + idea.resource_uri)
  }
}
