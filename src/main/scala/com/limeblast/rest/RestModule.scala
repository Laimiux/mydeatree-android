package com.limeblast.rest

import com.limeblast.mydeatree.{App, Meta, DjangoRootObject}

import java.util
import org.apache.http.HttpResponse

import java.io.InputStream
import org.apache.http.entity.StringEntity
import android.util.Log

trait RestModule[Obj, ObjCollection] extends JsonModule with HttpRequestModule {
  var RestModule_DEBUG = true


  private val MODULE_TAG = "RestModule"

  def api_url: String
  def resource_name: String
  def username: String
  def password: String
  def objType: Class[Obj]
  def collectionType: Class[ObjCollection]

  def getObject(url: String): Option[Obj] = {
    getFromUrl(username, password, url) match {
      case Some(response) => {
        val content: InputStream = response.getEntity.getContent

        val obj = getMainObject(content, objType)
        Some(obj)
      }
      case None => None
    }
  }


  protected def handleCollectionMeta(objects: util.ArrayList[Obj], collection: ObjCollection) = {}

  def collectionToList(collection: ObjCollection): util.ArrayList[Obj]


  def getObjects[T](): Option[util.ArrayList[Obj]] = getObjects(api_url + resource_name)

  def getObjects[T](initialUrl: String): Option[util.ArrayList[Obj]] = {
    getFromUrl(username, password, initialUrl) match {
      case Some(response) => {
        if (isResponseOkay(response)) {
          val content = response.getEntity.getContent

          val mainObject = getMainObject(content, collectionType)

          //  Convert Object Collection to ArrayList
          val objects = collectionToList(mainObject)

          // Handle meta
          handleCollectionMeta(objects, mainObject)


          Some(objects)
        } else {

          // Add a hook to handle error responses
          None
        }
      }
      case _ => None
    }
  }


  def postObject(originalObject: Obj): Option[Obj] = postObject(api_url + resource_name, originalObject)


  def postObject(url: String, originalObject: Obj): Option[Obj] =
    try {
      val jsonString = convertObjectToJson(originalObject)

      //Log.d(APP_TAG, jsonString)

      val httpClient = getHttpClientWithCredentials(username, password)
      val post = HttpPostWithJson(url, new StringEntity(jsonString))
      val response = httpClient.execute(post)

      if (response != null && isPostResponseOkay(response)) {
        val finalObject = getMainObject(response.getEntity.getContent, objType)
        Some(finalObject)
      } else {
        if (RestModule_DEBUG) Log.d(MODULE_TAG, "postObject was unsuccessful.")
        None
      }
    } catch {
      case _: Throwable => None
    }



  private def constructObjectUrl(obj: Obj): String = {
    type R = AnyRef { def id: String }
    type K = AnyRef { def resource_uri: String }
    obj match {
      case obj: R => api_url + resource_name + obj.id
      case obj: K => api_url + obj.resource_uri
      case _ => throw new IllegalAccessException("An object that you passed needs to have an id or a resource_uri. Construct a url and use a method which accepts the url instead")
    }
  }


  def deleteObject(obj: Obj): Boolean = deleteObject(constructObjectUrl(obj))

  /**
   * Delete an object at specific url
   * @param url Internet address
   * @return True if object was successfully deleted
   */
  def deleteObject(url: String): Boolean = {
    Log.d(MODULE_TAG, "Deleting an object at " + url)
    deleteFromUrl(username, password, url) match {
      case Some(response) if (isResponseDelete(response)) => true
      case _ => false
    }
  }

  def updateObject(objectToUpdate: Obj): Option[Obj] = {
    val url = constructObjectUrl(objectToUpdate)
    if(RestModule_DEBUG) Log.d(MODULE_TAG, "Updating object at " + url)
    updateObject(url, objectToUpdate)
  }

  def updateObject(url: String, updatedObject: Obj): Option[Obj] =
    try {
      val jsonString = convertObjectToJson(updatedObject)

      val httpClient = getHttpClientWithCredentials(username, password)
      val put = getPutWithJson(url, new StringEntity(jsonString))
      val response = httpClient.execute(put)

      if (response != null && isPutResponseOkay(response)) {
        // Parse response object
        val finalObject = getMainObject(response.getEntity.getContent, objType)
        Some(finalObject)
      } else {
        None
      }
    } catch {
      case e: Exception => {
        if(RestModule_DEBUG) Log.d(MODULE_TAG, e.toString)
        None
      }
    }


  private def isPutResponseOkay(response: HttpResponse): Boolean = {
    val statusCode = response.getStatusLine.getStatusCode
    if (statusCode == 204 || statusCode == 202 || statusCode == 200) {
      true
    } else {
      if(RestModule_DEBUG) Log.d(MODULE_TAG, "Put request failed, status " + statusCode)
      return false
    }
  }

  private def isPostResponseOkay(response: HttpResponse): Boolean = {
    val statusCode = response.getStatusLine.getStatusCode
    if (statusCode == 201) {
      true
    } else {
      if(RestModule_DEBUG) Log.d(MODULE_TAG, "Post request failed, status " + statusCode)
      false
    }
  }

  private def isResponseOkay(response: HttpResponse): Boolean = {
    val statusCode = response.getStatusLine.getStatusCode
    if (statusCode == 200) {
      true
    } else {

      if(RestModule_DEBUG) Log.d(MODULE_TAG, "Request failed, status " + statusCode)
      return false
    }
  }

  private def isResponseDelete(response: HttpResponse): Boolean = {
    val statusCode = response.getStatusLine.getStatusCode
    if (statusCode == 204 || statusCode == 404 || statusCode == 200 || statusCode == 410) {
      true
    } else {
      if(RestModule_DEBUG) Log.d("RestModule", "Delete request failed, status " + statusCode)
      return false
    }
  }
}

