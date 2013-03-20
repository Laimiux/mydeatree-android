package com.limeblast.androidhelpers

import com.limeblast.mydeatree.{Meta, DjangoRootObject}

import java.util
import org.apache.http.HttpResponse

import java.io.InputStream
import org.apache.http.entity.StringEntity
import android.util.Log

trait RestModule[Obj, ObjCollection] extends JsonModule with HttpRequestModule {
  var RestModule_DEBUG = true


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
        None
      }
    } catch {
      case _: Throwable => None
    }


  def deleteObject(obj: Obj): Boolean = {
    type r = AnyRef { def id: String }
    obj match {
      case obj: r => deleteObject(api_url + resource_name + obj.id)
      case obj: { def resource_uri: String } => deleteObject(api_url + obj.resource_uri)
      case _ => throw new IllegalAccessException("An object you are deleting needs to have id or resource_url. Use deleteObject(url: String) instead")
    }
   }

  def deleteObject(url: String): Boolean = {
    Log.d("RestModule", "Deleting an object at " + url)
    deleteFromUrl(username, password, url) match {
      case Some(response) if (isResponseDelete(response)) => true
      case _ => false
    }
  }

  def putObject[T](url: String, updatedObject: Obj): Option[Obj] =
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
        //if(AppSettings.DEBUG) Log.d(APP_TAG, e.toString)
        None
      }
      case _: Throwable => None
    }


  private def isPutResponseOkay(response: HttpResponse): Boolean = {
    val statusCode = response.getStatusLine.getStatusCode
    if (statusCode == 204 || statusCode == 202) {
      true
    } else {
      //if(AppSettings.DEBUG) Log.d(APP_TAG, "Request failed, status " + response.getStatusLine.getStatusCode)
      return false
    }
  }

  private def isPostResponseOkay(response: HttpResponse): Boolean = {
    if (response.getStatusLine.getStatusCode == 201) {
      true
    } else {
      //if(AppSettings.DEBUG) Log.d(APP_TAG, "POST Request failed")
      false
    }
  }

  private def isResponseOkay(response: HttpResponse): Boolean = {
    if (response.getStatusLine.getStatusCode == 200) {
      true
    } else {
      //if(AppSettings.DEBUG) Log.d(APP_TAG, "Request failed, status " + response.getStatusLine.getStatusCode)
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




/*
def retrieveObjects[T](initialUrl: String, objectKind: Class[T]): Option[util.ArrayList[AnyRef]] = {
  val objects: util.ArrayList[AnyRef] = new util.ArrayList()

  getFromUrl(username, password, initialUrl) match {
    case Some(response) => {
      if (isResponseOkay(response)) {
        val content: InputStream = response.getEntity.getContent

        val mainObject: DjangoRootObject[AnyRef] = getMainObject(content, objectKind).asInstanceOf[DjangoRootObject[AnyRef]]

        val meta: Meta = mainObject.meta

        objects.addAll(mainObject.objects)

        if (meta.next != null) {
          retrieveObjects(api_url + meta.next, objectKind) match {
            case Some(moreObjects) => objects.addAll(moreObjects)
            case None =>
          }
        }

        Some(objects)
      } else {
        None
      }
    }
    case None => {
      //if(AppSettings.DEBUG) Log.d(APP_TAG, "Received a null response")
      None
    }
  }
}

*/
/*

def retrieveObject[T](url: String, objClass: Class[T]): Option[T] = {
  getFromUrl(username, password, url) match {
    case Some(response) => {
      val content: InputStream = response.getEntity.getContent

      val obj: T = getMainObject(content, objClass)
      Some(obj)
    }
    case None => None
  }
}
*/
