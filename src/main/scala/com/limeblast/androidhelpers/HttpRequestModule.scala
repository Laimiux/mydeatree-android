package com.limeblast.androidhelpers

import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.params.HttpConnectionParams
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.HttpResponse
import org.apache.http.client.methods.{HttpPost, HttpPut, HttpDelete, HttpGet}
import android.util.Log
import org.apache.http.entity.StringEntity

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 3/11/13
 * Time: 10:52 PM
 * To change this template use File | Settings | File Templates.
 */
trait HttpRequestModule {
  var HttpRequestModule_DEBUG = true
  /**
   * Create or retrieve HttpClient with provided credentials
   * @param user Username for authentication
   * @param pw   Password for authentication
   * @return DefaultHttpClient with specified credentials
   */
  def getHttpClientWithCredentials(user: String, pw: String): DefaultHttpClient = {
    val defaultClient = new DefaultHttpClient()
    HttpConnectionParams.setSoTimeout(defaultClient.getParams(), 25000)
    val creds = new UsernamePasswordCredentials(user, pw)
    defaultClient.getCredentialsProvider.setCredentials(AuthScope.ANY, creds)

    defaultClient
  }

  private def createHttpClient(user: String, pw: String, timeout: Int): DefaultHttpClient = {
    val defaultClient = new DefaultHttpClient()
    HttpConnectionParams.setSoTimeout(defaultClient.getParams(), timeout)
    val creds = new UsernamePasswordCredentials(user, pw)
    defaultClient.getCredentialsProvider.setCredentials(AuthScope.ANY, creds)

    defaultClient
  }


  def getFromUrl(user: String, pw: String, url: String): Option[HttpResponse] =
    try {
      val httpclient = getHttpClientWithCredentials(user, pw)
      val get = new HttpGet(url)
      get.addHeader("accept", "application/json")
      Some(httpclient.execute(get))
    } catch {
      case e: Exception => {
        //if(AppSettings.DEBUG) Log.d(APP_TAG, e.toString)
        None
      }
      case _: Throwable => None
    }



  def deleteFromUrl(username: String, password: String, url: String): Option[HttpResponse] =
    try {
      if(HttpRequestModule_DEBUG)
        Log.d("HttpRequestModule", "Attempting to delete resource at " + url)
      val httpClient = createHttpClient(username, password, 3000)
      val del = new HttpDelete(url)
      Some(httpClient.execute(del))
    } catch {
      case e: Exception => {
        if(HttpRequestModule_DEBUG) Log.d("HttpRequestModule", "Delete error : " + e.toString)
        None
      }
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


