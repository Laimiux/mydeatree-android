package com.limeblast.mydeatree.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.content.{Intent, SharedPreferences}
import org.apache.http.client.methods.HttpGet
import android.util.Log

import android.widget.{EditText, Toast}

import android.preference.PreferenceManager
import com.actionbarsherlock.app.SherlockActivity

import concurrent.ops._
import android.net.Uri

import com.limeblast.androidhelpers._
import AndroidImplicits.{toListener, functionToResultReceicer}
import com.actionbarsherlock.view.Window
import com.limeblast.mydeatree._
import com.limeblast.mydeatree.AppSettings._
import services.PrivateIdeaSyncService
import android.content.res.Configuration


class LoginActivity extends SherlockActivity with TypedActivity with JsonModule with HttpRequestModule {

  var dialog: ProgressDialog = null

  // change these to lazy vals
  lazy val userField: EditText = findView(TR.username_edit)
  lazy val passwordField: EditText = findView(TR.password_edit)


  private val LOGIN_MESSAGE = "Signing in. Please wait..."
  private val SYNC_MESSAGE = "Syncing. Please wait..."

  private var loggingIn = false
  private var isSyncing = false

  private val BUNDLE_LOGGING_IN = "IS_LOGGING_IN"
  private val BUNDLE_SYNCING = "IS_SYNCING"

  lazy val loginBtn = findView(TR.login_button)

  val mHandler = new ScalaHandler()

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)

    requestWindowFeature(Window.FEATURE_NO_TITLE)

    setContentView(R.layout.login_layout)

    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
      findView(TR.login_header).setVisibility(View.GONE)


    if (bundle != null) {
      loggingIn = bundle.getBoolean(BUNDLE_LOGGING_IN, false)
      isSyncing = bundle.getBoolean(BUNDLE_SYNCING, false)
    }

    if (loggingIn) {
      startProgressBar(LOGIN_MESSAGE)
    } else if (isSyncing) {
      startProgressBar(SYNC_MESSAGE)
    }


    // Get the Login Button and add listener to it.
    loginBtn.setOnClickListener((view: View) => {
      startProgressBar(LOGIN_MESSAGE)
      loggingIn = true

      spawn {
        login_user()
      }
    })

    // Set listener for forgot password link
    findView(TR.forgot_password_link).setOnClickListener((view: View) => {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.forgot_password_link))))
    })

    // Set listener for register link
    findView(TR.link_to_register).setOnClickListener((view: View) => {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.registration_link))))
    })

  }


  override def onResume() {
    super.onResume()
    if (!AndroidHelpers.isOnline(this)) {
      loginBtn.setClickable(false)
      loginBtn.setText(R.string.no_connection)
    } else {
      loginBtn.setClickable(true)
      loginBtn.setText(R.string.login_button)
    }
  }

  protected override def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putBoolean(BUNDLE_LOGGING_IN, loggingIn)
    outState.putBoolean(BUNDLE_SYNCING, isSyncing)
  }

  def startProgressBar(text: String) {
    dialog = ProgressDialog.show(this, "", text, true)
  }

  // Function to login user.

  // REFACTOR THIS BIATCH
  private def login_user() {
    try {
      // User and pw
      val user = userField.getText.toString
      val pw = passwordField.getText.toString

      if (user.equals("") || pw.equals("")) {
        mHandler.post(loginInfo)
      } else {

        // Create http client and set credentials
        val httpclient = getHttpClientWithCredentials(user, pw)

        // Set get method
        val get = new HttpGet(App.getApi() + "api/v1/user/")
        get.addHeader("accept", "application/json")

        try {
          val response = httpclient.execute(get)
          val statusCode = response.getStatusLine.getStatusCode

          if (AppSettings.DEBUG) Log.d("Status code", "" + statusCode)

          if (statusCode == 200) {
            App.saveUser(this, userField.getText.toString, passwordField.getText.toString)

            //savePreferences()


            mHandler.post(successLogin)

            val users: Users = getMainObject(response.getEntity.getContent, classOf[Users])
            val usr = users.objects.get(0)

            if (AppSettings.DEBUG)
              Log.d("Mydea", "User firstname " + usr.first_name + " username is " +
                usr.username + " last name " + usr.last_name + " resource url " + usr.resource_uri)


          } else if (statusCode == 401) {
            mHandler.post(loginDenied)
          }


        } catch {
          case e: Exception => e.printStackTrace()
        }
      }
    }
  }

  private def successLogin() {
    // Remove old loader
    removeLoader()

    loggingIn = false
    isSyncing = true

    Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_LONG).show()

    // Create a dialog that private ideas are syncing
    startProgressBar(SYNC_MESSAGE)


    //val int: Intent = (LoginActivity.this, classOf[PrivateIdeaSyncService])
    // Once logged in start syncing private ideas
    val intent = new Intent(LoginActivity.this, classOf[PrivateIdeaSyncService])
    intent.putExtra(PRIVATE_IDEA_RESULT_RECEIVER, (resultCode: Int, resultData: Bundle) => {
      if (resultCode == 0) {
        // Forward to main activity
        val intent = new Intent()
        intent.setClass(LoginActivity.this, classOf[MainActivity])
        startActivity(intent)
        finish()
      }
    })

    startService(intent)
  }

  private def loginInfo() {
    // Remove loader
    removeLoader()
    Toast.makeText(LoginActivity.this, "Enter username and password!", Toast.LENGTH_LONG).show()
  }

  private def loginDenied() {
    // Remove loader
    removeLoader()
    Toast.makeText(LoginActivity.this, "Wrong username or password", Toast.LENGTH_LONG).show()
  }

  private def removeLoader() {
    if (dialog != null) {
      try {
        dialog.dismiss()
        dialog = null

        loggingIn = false
      }
    }
  }


  protected override def onDestroy() {
    super.onDestroy()
    removeLoader()
  }
}
