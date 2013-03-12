package com.limeblast.mydeatree

import android.content.Context
import android.preference.PreferenceManager

object App extends MydeaRestModule {


  val PREF_USERNAME = "PREF_USERNAME"
  val PREF_PASSWORD = "PREF_PASSWORD"

  /*
   * Safe way to access username
   */
  def getUsername(context: Context): String = {
    if (USERNAME.equals("")) {
      val prefs = PreferenceManager.getDefaultSharedPreferences(context)
      USERNAME = prefs.getString(PREF_USERNAME, "")
    }
    USERNAME
  }

  def isLoggedIn(): Boolean = {
    if (USERNAME.equals("")) {
      false
    } else {
      true
    }
  }

  def isLoggedIn(context: Context): Boolean = {
    getUsername(context)
    isLoggedIn()
  }
}

/*

  package com.limeblast.mydeatree

import android.content.Context
import android.preference.PreferenceManager

/**
 * A singleton object
 * that holds important
 * application settings
 */
object AppSettings {
  val APP_TAG = "MydeaTree"
  val PREFS_NAME = "MydeaPrefs"

  val DATE_FORMAT = "%F %T"

  //"yyyy-MM-dd'T'HH:mm:ss"
  val PYTHON_DATE_FORMAT = "%FT%T"
  // Just use date for sync
  // Currently only date is kept
  //val DATE_FORMAT = "%F"

  val PREF_UPDATE_FREQ_INDEX = "PREF_UPDATE_FREQ_INDEX"
  val PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE"
  val PREF_USERNAME = "PREF_USERNAME"
  val PREF_PASSWORD = "PREF_PASSWORD"
  val PREF_LAST_PRIVATE_IDEAS_SYNCED = "PREF_LAST_PRIVATE_IDEAS_SYNCED"
  // USE this with USERNAME_ in front
  val PREF_PRIVATE_SYNC = "PREF_PRIVATE_SYNC"
  val PREF_PUBLIC_SORT = "PREF_PUBLIC_SORT"
  val PREF_PRIVATE_SORT = "PREF_PRIVATE_SORT"

  val API_URL = "https://mydeatree.appspot.com"
  val USER_URL = API_URL + "/api/v1/user/"
  val IDEA_URL = API_URL + "/api/v1/idea/"
  val PUBLIC_IDEA_URL = API_URL + "/api/v1/public_ideas/"
  val FAVORITE_IDEAS_URL = API_URL + "/api/v1/favorite_ideas/"

  val PRIVATE_IDEA_RESULT_RECEIVER = "PRIVATE_IDEA_RESULT_RECEIVER"
  val PUBLIC_IDEA_RESULT_RECEIVER = "PUBLIC_IDEA_RESULT_RECEIVER"
  val IDEA_UPDATE_RESULT_RECEIVER = "IDEA_UPDATE_RESULT_RECEIVER"
  val IDEA_CREATE_RESULT_RECEIVER = "IDEA_CREATE_RESULT_RECEIVER"

  val AUTO_UPDATE = true
  var USERNAME = ""
  var PASSWORD = ""
  var LAST_PRIVATE_IDEAS_SYNCED = ""

  // State of Sync If Necessary
  var IS_PERSONAL_IDEAS_SYNCING = false
  // STATE of idea tree hierarchy
  var PRIVATE_PARENT_IDEA: Option[Idea] = None

  val LOGIN_ACTIVITY = 1
  val REGISTER_ACTIVITY = 2
  val SHOW_PREFERENCES = 3
  val NEW_IDEA_ACTIVITY = 4

  // State of main acitivy
  var IS_MAIN_ACTIVITY_RUNNING = false

  // Notification variables
  val HAS_NEW_PUBLIC_IDEAS = "HAS_NEW_PUBLIC_IDEAS"



  val DEBUG = true

}


*/