package com.limeblast.mydeatree.services

import android.app.IntentService
import android.content.Intent
import android.os.ResultReceiver
import com.limeblast.mydeatree.App
import org.apache.http.client.methods.HttpGet
import com.limeblast.rest.HttpRequestModule
import android.util.Log
import org.apache.http.HttpResponse

object LoginService {
 val SUCCESS_LOGIN = 1000
 val FAILED_LOGIN = 2000
}

class LoginService extends IntentService("LoginService") with HttpRequestModule {

  def onHandleIntent(intent: Intent) {
    val username = intent.getStringExtra("username")
    val password = intent.getStringExtra("password")

    if (username == "" || password == "")
      throw new IllegalArgumentException("LoginService needs username and password to be passed through the intent.")

    val receiver: Option[ResultReceiver] = Option(intent.getParcelableExtra(App.LOGIN_RESULT_RECEIVER))


    receiver match {
      case None => throw new IllegalArgumentException("LoginService needs a result receiver to function")
      case Some(receiver) => handleLogin(receiver, username, password)
    }

  }

  private def handleLogin(receiver: ResultReceiver, username: String, password: String) = {
    if (login(username,password))
      receiver.send(LoginService.SUCCESS_LOGIN, null)
    else
      receiver.send(LoginService.FAILED_LOGIN, null)
  }


  private def login(username: String, password: String): Boolean = {
    // Create http client and set credentials
    val httpclient = getHttpClientWithCredentials(username, password)

    // Set get method
    val get = new HttpGet(App.getApi() + "/api/v1/user/")
    get.addHeader("accept", "application/json")

    val response = try {
      Some(httpclient.execute(get))
    } catch {
      case e:Exception => {
        if (App.DEBUG) Log.d("LoginService", "Error " + e.toString)
        None
      }
    }

    response match {
      case None => false
      case Some(response) => isResponseSuccessful(response)
    }
  }


  private def isResponseSuccessful(response: HttpResponse): Boolean = {

    /*  Code that can parse the user
       val users: Users = getMainObject(response.getEntity.getContent, classOf[Users])
            val usr = users.objects.get(0)

            if (App.DEBUG)
              Log.d("Mydea", "User firstname " + usr.first_name + " username is " +
                usr.username + " last name " + usr.last_name + " resource url " + usr.resource_uri)

     */
    val statusCode = response.getStatusLine.getStatusCode

    if (statusCode == 200) true
    else false
  }
}
