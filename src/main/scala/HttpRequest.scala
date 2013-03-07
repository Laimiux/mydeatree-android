package com.limeblast.mydeatree

import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.methods.{HttpPut, HttpDelete, HttpPost, HttpGet}
import org.apache.http.entity.StringEntity
import org.apache.http.HttpResponse

import android.util.Log
import org.apache.http.params.HttpConnectionParams

object HttpRequest {
  val APP_TAG = "HTTP_REQUEST_WRAPPER"

  /**
   * Create or retrieve HttpClient with provided credentials
   * @param user Username for authentication
   * @param pw   Password for authentication
   * @return DefaultHttpClient with specified credentials
   */
  def getHttpClientWithCredentials(user: String, pw: String): DefaultHttpClient = {
    val defaultClient = new DefaultHttpClient()
    HttpConnectionParams.setSoTimeout(defaultClient.getParams(), 25000);
    val creds = new UsernamePasswordCredentials(user, pw)
    defaultClient.getCredentialsProvider.setCredentials(AuthScope.ANY, creds)

    defaultClient
  }

  def getFromUrl(user: String, pw: String, url: String): Option[HttpResponse] = {
    val httpclient = getHttpClientWithCredentials(user, pw)

    val get = new HttpGet(url)
    get.addHeader("accept", "application/json")

    val response: Option[HttpResponse] = try {
      Some(httpclient.execute(get))
    } catch {
      case e: Exception => {
        if(AppSettings.DEBUG) Log.d(APP_TAG, e.toString)
        None
      }
      case _ => {
        None
      }
    }

    response
  }

  def deleteFromUrl(username: String, password: String, url: String): Option[HttpResponse] = {
    if(AppSettings.DEBUG)
      Log.d(APP_TAG, "Attempting to delete an idea from " + url)

    val httpClient = getHttpClientWithCredentials(username, password)

    val del = new HttpDelete(url)

    val response: Option[HttpResponse] = try {
      Some(httpClient.execute(del))
    } catch {
      case e: Exception => {
        if(AppSettings.DEBUG) Log.d(APP_TAG, e.toString)
        None
      }
      case _ => None
    }

    response
  }

  def getPutWithJson(url: String, body: StringEntity): HttpPut = {
    val put = new HttpPut(url)
    put.addHeader("accept", "application/json")
    put.addHeader("Content-Type", "application/json")
    put.setEntity(body)

    put
  }

  def HttpPostWithJson(url: String): HttpPost = {
    val post = new HttpPost(url)
    post.addHeader("accept", "application/json")
    post.addHeader("Content-Type", "application/json")

    post
  }

  def HttpPostWithJson(url: String, body: StringEntity): HttpPost = {
    val post = HttpPostWithJson(url)
    post.setEntity(body)
    post
  }
}
