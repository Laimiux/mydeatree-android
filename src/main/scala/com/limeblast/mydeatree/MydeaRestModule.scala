package com.limeblast.mydeatree

import com.limeblast.androidhelpers.RestModule
import android.util.Log

import java.util

trait MydeaRestModule {
  private val APP_TAG = "MydeaRestModule"

  def getApi(): String = "https://mydeatree.appspot.com"

  var USERNAME = ""
  var PASSWORD = ""

  object PersonalIdeaResource extends RestModule {
    def api_url: String = getApi()
    def username: String = USERNAME
    def password: String = PASSWORD

    def retrieveIdeas(initialUrl: String): Option[util.ArrayList[Idea]] = {
      retrieveObjects(initialUrl, classOf[PersonalIdeas]) match {
        case Some(ideas: util.ArrayList[AnyRef]) => {
          if (AppSettings.DEBUG) Log.d(APP_TAG, "Retrieved " + ideas.size() + " ideas")
          Some(ideas.asInstanceOf[util.ArrayList[Idea]])
        }
        case None => None
      }
    }


    def getIdea(url: String): Option[Idea] = retrieveObject(url, classOf[Idea])

    def postIdea(url: String, idea: Idea): Option[Idea] = postObject(url, idea)

    def updateIdea(newIdea: Idea): Option[Idea] = putObject(AppSettings.API_URL + newIdea.resource_uri, newIdea)

    def deleteIdea(idea: Idea): Boolean = {
      if (AppSettings.DEBUG) Log.d(APP_TAG, "Deleting an idea")
      deleteObject(AppSettings.API_URL + idea.resource_uri)
    }
  }


  object PublicIdeaResource extends RestModule {
    def api_url: String = getApi()
    def username: String = USERNAME
    def password: String = PASSWORD

    def retrievePublicIdeas(initialUrl: String): Option[util.ArrayList[PublicIdea]] =
      retrieveObjects(initialUrl, classOf[PublicIdeas]) match {
        case Some(objects) => Some(objects.asInstanceOf[util.ArrayList[PublicIdea]])
        case None => None
      }
  }
}
