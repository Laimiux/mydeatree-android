package com.limeblast.mydeatree

/**
 * A singleton object
 * that holds important
 * application settings
 */

/* this will be moved to
App objects
 */
object AppSettings {
  val APP_TAG = "MydeaTree"

  val DATE_FORMAT = "%F %T"

  //"yyyy-MM-dd'T'HH:mm:ss"
  val PYTHON_DATE_FORMAT = "%FT%T"
  // Just use date for sync
  // Currently only date is kept
  //val DATE_FORMAT = "%F"

  val PREF_UPDATE_FREQ_INDEX = "PREF_UPDATE_FREQ_INDEX"
  val PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE"


  val PREF_LAST_PRIVATE_IDEAS_SYNCED = "PREF_LAST_PRIVATE_IDEAS_SYNCED"
  // USE this with USERNAME_ in front
  val PREF_PRIVATE_SYNC = "PREF_PRIVATE_SYNC"
  val PREF_PUBLIC_SORT = "PREF_PUBLIC_SORT"
  val PREF_PRIVATE_SORT = "PREF_PRIVATE_SORT"


  val PRIVATE_IDEA_RESULT_RECEIVER = "PRIVATE_IDEA_RESULT_RECEIVER"
  val PUBLIC_IDEA_RESULT_RECEIVER = "PUBLIC_IDEA_RESULT_RECEIVER"
  val IDEA_UPDATE_RESULT_RECEIVER = "IDEA_UPDATE_RESULT_RECEIVER"
  val IDEA_CREATE_RESULT_RECEIVER = "IDEA_CREATE_RESULT_RECEIVER"

  val AUTO_UPDATE = true

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

}

