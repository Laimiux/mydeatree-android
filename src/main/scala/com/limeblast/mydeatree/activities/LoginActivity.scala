package com.limeblast.mydeatree.activities

import android.app.ProgressDialog
import android.os.{Handler, Bundle}
import android.view.View
import android.content.Intent

import android.widget.EditText

import com.actionbarsherlock.app.SherlockActivity

import android.net.Uri

import com.limeblast.androidhelpers.ScalifiedActivity
import com.actionbarsherlock.view.Window
import com.limeblast.mydeatree._
import com.limeblast.mydeatree.AppSettings._
import services.{LoginService, PrivateIdeaSyncService}
import android.content.res.Configuration
import com.limeblast.rest.{JsonModule, HttpRequestModule}
import android.util.Log


class LoginActivity extends SherlockActivity with TypedActivity
with JsonModule with HttpRequestModule with ScalifiedActivity {

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

  var handler: Handler = _

  //-------------------------------------------------------\\
  //------------ ACTIVITY LIFECYCLE EVENTS ----------------\\
  //-------------------------------------------------------\\
  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)

    requestWindowFeature(Window.FEATURE_NO_TITLE)

    setContentView(R.layout.login_layout)

    handler = new Handler()


    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
      findView(TR.login_header).setVisibility(View.GONE)


    if (bundle != null) {
      loggingIn = bundle.getBoolean(BUNDLE_LOGGING_IN, false)
      isSyncing = bundle.getBoolean(BUNDLE_SYNCING, false)
    }

    if (loggingIn) {
      startProgressBar(LOGIN_MESSAGE)
      startObservationalThread()
    } else if (isSyncing) {
      startProgressBar(SYNC_MESSAGE)
      startObservationalThread()
    }

    // Get the Login Button and add listener to it.
    loginBtn.onClick({
      val username = userField.getText.toString
      val password = passwordField.getText.toString

      loginButtonClick(username, password)
    })


    // Set listener for forgot password link
    findView(TR.forgot_password_link).onClick({
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.forgot_password_link))))
    })

    // Set listener for register link
    findView(TR.link_to_register).onClick({
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.registration_link))))
    })

  }


  override def onResume() {
    super.onResume()


    if (!isOnline(this)) {
      loginBtn.setClickable(false)
      loginBtn.setText(R.string.no_connection)
    } else {
      loginBtn.setClickable(true)
      loginBtn.setText(R.string.login_button)
    }
  }

  protected override def onDestroy() {
    super.onDestroy()
    removeLoader()

  }

  protected override def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putBoolean(BUNDLE_LOGGING_IN, loggingIn)
    outState.putBoolean(BUNDLE_SYNCING, isSyncing)
  }


  //-------------------------------------------------------\\
  //----------- STATE CHECKING FUNCTIONS ------------------\\
  //-------------------------------------------------------\\
  private def startObservationalThread() = {
    new Thread(new Runnable() {
      def run() {
        while(true){
          if (loggingIn && !isLoginServiceRunning) {
            loggingIn = false
            if (!isSyncServiceRunning()) {
              removeLoader()
            } else {
              isSyncing = true
            }

            return
          }
          else if (isSyncing && !isSyncServiceRunning()) {
            isSyncing = false
            removeLoader()
            return
          }



          if (App.DEBUG) Log.d("LoginActivity", "observational thread is running")

          Thread.sleep(2000)
        }
      }
    }).start()
  }


  private def isLoginServiceRunning(): Boolean = isServiceRunning(classOf[LoginService].getName)

  private def isSyncServiceRunning(): Boolean = isServiceRunning(classOf[PrivateIdeaSyncService].getName)

  //-------------------------------------------------------\\
  //---------------- LOGIN FUNCTIONS ----------------------\\
  //-------------------------------------------------------\\
  private def loginButtonClick(username: String, password: String) {
    if (username == "" || password == "")
      loginParametersMissing()
    else {
      startProgressBar(LOGIN_MESSAGE)
      setLoginState(logging_in = true, syncing = false)
      loginUser(username, password)
    }
  }

  private def loginUser(username: String, password: String) {
    val intent = new Intent(this, classOf[LoginService])
    intent.putExtra("username", username)
    intent.putExtra("password", password)
    intent.putExtra(App.LOGIN_RESULT_RECEIVER, (resultCode: Int, resultData: Bundle) => {
      resultCode match {
        case LoginService.SUCCESS_LOGIN => handler.post(successLogin(username, password))
        case LoginService.FAILED_LOGIN => handler.post(loginDenied)
      }
    })

    startService(intent)

  }


  private def setLoginState(logging_in: Boolean, syncing: Boolean) {
    loggingIn = logging_in
    isSyncing = syncing
  }


  //-------------------------------------------------------\\
  //------------ LOGIN EVENT HANDLERS ---------------------\\
  //-------------------------------------------------------\\


  private def successLogin(username: String, password: String) {

    saveUser(username, password)

    removeLoader()

    setLoginState(logging_in = false, syncing = true)

    longToast("Successfully logged in")


    // Create a dialog that private ideas are syncing
    startProgressBar(SYNC_MESSAGE)


    //val int: Intent = (LoginActivity.this, classOf[PrivateIdeaSyncService])
    // Once logged in start syncing private ideas
    val intent = new Intent(LoginActivity.this, classOf[PrivateIdeaSyncService])
    intent.putExtra(PRIVATE_IDEA_RESULT_RECEIVER, (resultCode: Int, resultData: Bundle) => {
      if (resultCode == 0) {
        // Forward to main activity
        startActivity(classOf[MainActivity])
        finish()
      }
    })

    startService(intent)
  }

  private def loginParametersMissing() {
    setLoginState(logging_in = false, syncing = false)
    longToast("Enter username and password!")
  }

  private def loginDenied() {
    setLoginState(logging_in = false, syncing = false)
    // Remove loader
    removeLoader()
    longToast("Wrong username or password")
  }


  //-------------------------------------------------------\\
  //---------- PROGRESS BAR CONTROL FUNCTIONS -------------\\
  //-------------------------------------------------------\\
  def startProgressBar(text: String) {
    dialog = ProgressDialog.show(this, "", text, true)
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


  //-------------------------------------------------------\\
  //---------- Save User To SharedPreferences -------------\\
  //-------------------------------------------------------\\
  def saveUser(username: String, password: String) {
    App.USERNAME = username
    App.PASSWORD = password

    // Save login information to SharedPrefs
    val editor = getDefaultPreferences().edit()
    editor.putString(App.PREF_USERNAME, App.USERNAME)
    editor.putString(App.PREF_PASSWORD, App.PASSWORD)
    editor.commit()
  }
}
