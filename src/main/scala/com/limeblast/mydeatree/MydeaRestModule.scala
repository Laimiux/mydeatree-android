package com.limeblast.mydeatree

import com.limeblast.androidhelpers.RestModule
import android.util.Log

import java.util

trait MydeaRestModule {
  private val APP_TAG = "MydeaRestModule"

  def getApi(): String = "https://mydeatree.appspot.com"

  var USERNAME = ""
  var PASSWORD = ""

  object PersonalIdeaResource extends RestModule[Idea, PersonalIdeas] {
    def api_url: String = getApi()
    def username: String = USERNAME
    def password: String = PASSWORD


    val objType: Class[Idea] = classOf[Idea]
    val collectionType: Class[PersonalIdeas] = classOf[PersonalIdeas]


    override protected def handleCollectionMeta(objects: util.ArrayList[Idea], collection: PersonalIdeas) {
      val meta = collection.meta
      if (meta.next != null) {
        getObjects(api_url + meta.next) match {
          case Some(moreObjects) => objects.addAll(moreObjects)
          case None =>
        }
      }
    }

    def collectionToList(collection: PersonalIdeas): util.ArrayList[Idea] = new util.ArrayList(collection.objects)


    def updateIdea(newIdea: Idea): Option[Idea] = putObject(AppSettings.API_URL + newIdea.resource_uri, newIdea)

    def deleteIdea(idea: Idea): Boolean = {
      if (AppSettings.DEBUG) Log.d(APP_TAG, "Deleting an idea")
      deleteObject(AppSettings.API_URL + idea.resource_uri)
    }
  }


  object PublicIdeaResource extends RestModule[PublicIdea, PublicIdeas] {
    def api_url: String = getApi()
    def username: String = USERNAME
    def password: String = PASSWORD

    val objType: Class[PublicIdea] = classOf[PublicIdea]
    val collectionType = classOf[PublicIdeas]


    override protected def handleCollectionMeta(objects: util.ArrayList[PublicIdea], collection: PublicIdeas) {
      val meta = collection.meta
      if (meta.next != null) {
        getObjects(api_url + meta.next) match {
          case Some(moreObjects) => objects.addAll(moreObjects)
          case None =>
        }
      }
    }

    def collectionToList(collection: PublicIdeas): util.ArrayList[PublicIdea] = new util.ArrayList(collection.objects)
  }


  object FavoriteIdeaResource extends RestModule[FavoriteIdea, FavoriteIdeas] {
    def api_url: String = getApi()
    def username: String = USERNAME
    def password: String = PASSWORD


    val objType: Class[FavoriteIdea] = classOf[FavoriteIdea]
    val collectionType: Class[FavoriteIdeas] = classOf[FavoriteIdeas]

    def collectionToList(collection: FavoriteIdeas): util.ArrayList[FavoriteIdea] = new util.ArrayList(collection.objects)

    def deleteObject(obj: FavoriteIdea): Boolean = super.deleteObject(api_url + obj.resource_uri)

  }
}
