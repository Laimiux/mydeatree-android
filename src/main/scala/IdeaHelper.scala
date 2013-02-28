package com.limeblast.mydeatree

import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.content.Context
import android.util.Log

import AppSettings.APP_TAG

/**
 * Singleton object that holds important
 * variables about the database.
 */
object IdeaHelper {
  // Database specific info
  val DATABASE_NAME = "mydeatree.db"
  val DATABASE_VERSION = 1

  // For Idea Table
  val IDEA_TABLE_NAME = "ideas"

  // KEY Values
  val KEY_ID = "IDEA_ID"
  val KEY_OWNER = "IDEA_OWNER"
  val KEY_TITLE = "IDEA_TITLE"
  val KEY_TEXT = "IDEA_TEXT"
  val KEY_PARENT = "IDEA_PARENT"
  val KEY_PUBLIC = "IDEA_PUBLIC"
  val KEY_RESOURCE_URI = "IDEA_RESOURCE_URI"
  val KEY_CREATED_DATE = "IDEA_CREATED_DATE"
  val KEY_MODIFIED_DATE = "IDEA_MODIFIED_DATE"

  // Booleans for syncing
  val KEY_IS_IDEA_NEW = "IS_IDEA_NEW"
  val KEY_IS_IDEA_EDITED = "IS_IDEA_EDITED"
  val KEY_IS_IDEA_DELETED = "IS_IDEA_DELETED"
  val KEY_IS_IDEA_SYNCING = "IS_IDEA_SYNCING"
}

/**
 * Database Helper class for opening, creating and managing database version control
 * @param context Activity context
 */
class IdeaSQLiteHelper(context: Context) extends
SQLiteOpenHelper(context, IdeaHelper.DATABASE_NAME, null, IdeaHelper.DATABASE_VERSION) {

  import IdeaHelper._

  // SQL Statement to create ideas table
  val IDEA_TABLE_CREATE = "CREATE TABLE " + IDEA_TABLE_NAME + " (" +
    KEY_ID + " TEXT PRIMARY KEY UNIQUE, " +
    KEY_OWNER + " TEXT, " +
    KEY_RESOURCE_URI + " TEXT, " +
    KEY_CREATED_DATE + " TEXT, " +
    KEY_MODIFIED_DATE + " TEXT, " +
    KEY_TITLE + " TEXT not null, " +
    KEY_TEXT + " TEXT not null, " +
    KEY_PARENT + " TEXT, " +
    KEY_PUBLIC + " BOOLEAN default 0, " +
    KEY_IS_IDEA_NEW + " BOOLEAN default 0," +
    KEY_IS_IDEA_EDITED + " BOOLEAN default 0," +
    KEY_IS_IDEA_SYNCING + " BOOLEAN default 0," +
    KEY_IS_IDEA_DELETED + " BOOLEAN default 0 " + ");"


  // Called when no database exists in disk and
  override def onCreate(db: SQLiteDatabase) {
    db.execSQL(IDEA_TABLE_CREATE)
  }

  // Updating the database
  override def onUpgrade(db: SQLiteDatabase, oldVer: Int, newVer: Int) {
    Log.w(APP_TAG, "Upgrading from version " + oldVer + " to " + newVer
    + ", which will destroy all old data.")
    // Drop older tables if exists
    db.execSQL("DROP TABLE IF EXISTS " + IDEA_TABLE_NAME)
    // Create tables again
    onCreate(db)
  }
}
