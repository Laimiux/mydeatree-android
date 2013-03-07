package com.limeblast.mydeatree

import java.util

import AppSettings._
import android.util.Log
import org.apache.http.HttpResponse
import java.io.InputStream
import org.apache.http.entity.StringEntity


object RESTCalls {
  val APP_TAG = "RESTCalls Wrapper"

  def retrieveObjects[T](initialUrl: String, objectKind: Class[T]): Option[util.ArrayList[AnyRef]] = {
    val objects: util.ArrayList[AnyRef] = new util.ArrayList()

    //val response = HttpRequest.getFromUrl(USERNAME, PASSWORD, initialUrl)

    HttpRequest.getFromUrl(USERNAME, PASSWORD, initialUrl) match {
      case Some(response) => {
        if(isResponseOkay(response)) {
          val content: InputStream = response.getEntity.getContent

          val mainObject: DjangoRootObject[AnyRef] = JsonWrapper.getMainObject(content, objectKind).asInstanceOf[DjangoRootObject[AnyRef]]

          val meta: Meta = mainObject.meta

          objects.addAll(mainObject.objects)

          if (meta.next != null) {
            retrieveObjects(API_URL + meta.next, objectKind) match {
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
        if(AppSettings.DEBUG) Log.d(APP_TAG, "Received a null response")
        None
      }
    }
  }

  def retrieveObject[T](url: String, objClass: Class[T]): Option[T] = {
    HttpRequest.getFromUrl(USERNAME, PASSWORD, url) match {
      case Some(response) => {
        val content: InputStream = response.getEntity.getContent

        val obj: T = JsonWrapper.getMainObject(content, objClass)
        Some(obj)
      }
      case None => None
    }
  }

  def deleteObject(url: String): Boolean = {
    //val response =
    HttpRequest.deleteFromUrl(USERNAME, PASSWORD, url) match {
      case Some(response) if(isResponseDelete(response)) => true
      case _ => false
    }
  }

  def postObject[T](url: String, originalObject: T): Option[T] =
    try {
      val jsonString = JsonWrapper.convertObjectToJson(originalObject)

      Log.d(APP_TAG, jsonString)

      val httpClient = HttpRequest.getHttpClientWithCredentials(USERNAME, PASSWORD)
      val post = HttpRequest.HttpPostWithJson(url, new StringEntity(jsonString))
      val response = httpClient.execute(post)

      if (response != null && isPostResponseOkay(response)) {
        val finalObject = JsonWrapper.getMainObject(response.getEntity.getContent, originalObject.getClass)
        Some(finalObject)
      } else {
        None
      }
    } catch {
      case _ => None
    }


  def putObject[T](url: String, updatedObject: T): Option[T] =
    try {
      val jsonString = JsonWrapper.convertObjectToJson(updatedObject)

      val httpClient = HttpRequest.getHttpClientWithCredentials(USERNAME, PASSWORD)
      val put = HttpRequest.getPutWithJson(url, new StringEntity(jsonString))
      val response = httpClient.execute(put)

      if (response != null && isPutResponseOkay(response)) {
        // Parse response object
        val finalObject = JsonWrapper.getMainObject(response.getEntity.getContent, updatedObject.getClass)
        Some(finalObject)
      } else {
        None
      }
    } catch {
      case e: Exception => {
        if(AppSettings.DEBUG) Log.d(APP_TAG, e.toString)
        None
      }
      case _ => None
    }



  private def isPutResponseOkay(response: HttpResponse): Boolean = {
    val statusCode = response.getStatusLine.getStatusCode
    if (statusCode == 204 || statusCode == 202) {
      true
    } else {
      if(AppSettings.DEBUG) Log.d(APP_TAG, "Request failed, status " + response.getStatusLine.getStatusCode)
      return false
    }
  }

  private def isPostResponseOkay(response: HttpResponse): Boolean = {
    if (response.getStatusLine.getStatusCode == 201) {
      true
    } else {
      if(AppSettings.DEBUG) Log.d(APP_TAG, "POST Request failed")
      false
    }
  }

  private def isResponseOkay(response: HttpResponse): Boolean = {
    if (response.getStatusLine.getStatusCode == 200) {
      true
    } else {
      if(AppSettings.DEBUG) Log.d(APP_TAG, "Request failed, status " + response.getStatusLine.getStatusCode)
      return false
    }
  }

  private def isResponseDelete(response: HttpResponse): Boolean = {
    if (response.getStatusLine.getStatusCode == 204 || response.getStatusLine.getStatusCode == 404) {
      true
    } else {
      if(AppSettings.DEBUG) Log.d(APP_TAG, "Request failed, status " + response.getStatusLine.getStatusCode)
      return false
    }
  }

}