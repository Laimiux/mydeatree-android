package com.limeblast.mydeatree

import android.util.Log

import java.util
import com.limeblast.rest.RestModule
import com.limeblast.androidhelpers.{HasProvider, ProviderAccessModule}
import android.net.Uri
import providers.FavoriteIdeaProvider

trait MydeaRestModule {
  private val APP_TAG = "MydeaRestModule"

  def getApi(): String = "https://mydeatree.appspot.com"

  var USERNAME = ""
  var PASSWORD = ""

  object PersonalIdeaResource extends TastypieRestModule[Idea, PersonalIdeas] {
    def api_url: String = getApi()
    def username: String = USERNAME
    def password: String = PASSWORD

    val resource_name = "/api/v1/idea/"


    val objType: Class[Idea] = classOf[Idea]
    val collectionType: Class[PersonalIdeas] = classOf[PersonalIdeas]

  }


  object PublicIdeaResource extends TastypieRestModule[PublicIdea, PublicIdeas] {
    def api_url: String = getApi()
    val resource_name = "/api/v1/public_ideas/"
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

  }


  object FavoriteIdeaResource extends TastypieRestModule[FavoriteIdea, FavoriteIdeas]{
    def api_url: String = getApi()
    val resource_name = "/api/v1/favorite_ideas/"
    def username: String = USERNAME
    def password: String = PASSWORD


    val objType: Class[FavoriteIdea] = classOf[FavoriteIdea]
    val collectionType: Class[FavoriteIdeas] = classOf[FavoriteIdeas]

    object Provider extends HasProvider {
      val provider_uri: Uri = FavoriteIdeaProvider.CONTENT_URI
    }

  }
}
