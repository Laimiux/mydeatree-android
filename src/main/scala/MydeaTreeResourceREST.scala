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

  def retrieveIdeas(initialUrl: String): Option[util.ArrayList[Idea]] = {
    RESTCalls.retrieveObjects(initialUrl, classOf[PersonalIdeas]) match {
      case Some(ideas) => {
        if (AppSettings.DEBUG) Log.d(APP_TAG, "Retrieved " + ideas.size() + " ideas")
        Some(ideas.asInstanceOf[util.ArrayList[Idea]])
      }
      case None => None
    }
  }

  def retrievePublicIdeas(initialUrl: String): Option[util.ArrayList[PublicIdea]] =
    RESTCalls.retrieveObjects(initialUrl, classOf[PublicIdeas]) match {
      case Some(objects) => Some(objects.asInstanceOf[util.ArrayList[PublicIdea]])
      case None => None
    }


  def getIdea(url: String): Option[Idea] = RESTCalls.retrieveObject(url, classOf[Idea])

  def postIdea(url: String, idea: Idea): Option[Idea] = RESTCalls.postObject(url, idea)

  def updateIdea(newIdea: Idea): Option[Idea] = RESTCalls.putObject(AppSettings.API_URL + newIdea.resource_uri, newIdea)

  def deleteIdea(idea: Idea): Boolean = {
    if (AppSettings.DEBUG) Log.d(APP_TAG, "Deleting an idea")
    return RESTCalls.deleteObject(AppSettings.API_URL + idea.resource_uri)
  }
}
