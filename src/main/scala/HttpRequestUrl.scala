package com.limeblast.mydeatree

import java.io.InputStream
import java.net._
import android.util.Log
import com.limeblast.mydeatree.AppSettings._
import javax.net.ssl.HttpsURLConnection

object HttpRequestUrl {

  def getJsonFromUrl(link: String): InputStream = {

    var input: InputStream = null

    var url: URL = null
    var httpsConnection: HttpsURLConnection = null


    try {
      url = new URL(link)
      if(AppSettings.DEBUG)
        Log.d(APP_TAG, "Accessing url " + url)
      httpsConnection = url.openConnection().asInstanceOf[HttpsURLConnection]
      setAuthorization(USERNAME, PASSWORD)
      httpsConnection.setRequestMethod("GET")

      if(AppSettings.DEBUG)
        Log.d(APP_TAG, "Authorization header " + httpsConnection.getHeaderField("Authorization"))



      val responseCode = httpsConnection.getResponseCode
      if (responseCode == HttpURLConnection.HTTP_OK) {
        input = httpsConnection.getInputStream()
      } else {
        if(AppSettings.DEBUG)
          Log.d(APP_TAG, "Error with the HTTP call, response status is " + responseCode)
      }
    } catch {
      case me: MalformedURLException => {
        if(AppSettings.DEBUG)
          Log.d(APP_TAG, "Malformed error " + me.toString)
      }
      case e: Exception => Log.d(APP_TAG, "Error " + e.toString)
    } finally {

    }

    if(AppSettings.DEBUG) Log.d(APP_TAG, "Returning input stream is " + input)
    return input
  }

  def setAuthorization(username: String, password: String) {
    Authenticator.setDefault(new Authenticator(){
      override protected def getPasswordAuthentication: PasswordAuthentication = {
        return new PasswordAuthentication(username, password.toCharArray)
      }
    })
  }

}
