package com.limeblast.mydeatree

import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.content.Context

import com.limeblast.scaliteorm.{TableDefinition, DatabaseHelperTrait}

/**
 * Singleton object that holds important
 * variables about the database.
 */
object IdeaHelper {
  // Database specific info
  val DATABASE_NAME = "mydeatree.db"
  val DATABASE_VERSION = 7

  // For Idea Table
  val IDEA_TABLE_NAME = "personal_ideas"

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


object FavoriteIdeaColumns {
  val TABLE_NAME = "favorite_ideas"
  val KEY_ID = "id"
  val KEY_OWNER = "owner"
  val KEY_IDEA = "idea"
  val KEY_RESOURCE_URI = "resource_uri"
  val KEY_IS_DELETED = "is_deleted"
  val KEY_IS_NEW = "is_new"
}

/**
 * Database Helper class for opening, creating and managing database version control
 * @param context Activity context
 */
class IdeaSQLiteHelper(context: Context) extends
SQLiteOpenHelper(context, IdeaHelper.DATABASE_NAME, null, IdeaHelper.DATABASE_VERSION) with DatabaseHelperTrait {

  def tables: List[TableDefinition] = {
    var tempList: List[TableDefinition] = List()

    {
      import FavoriteIdeaColumns._
      val favorite_idea_table = new TableDefinition(TABLE_NAME)
      favorite_idea_table insert(KEY_ID -> "TEXT PRIMARY KEY UNIQUE",
        KEY_OWNER -> "TEXT not null",
        KEY_IDEA -> "TEXT not null",
        KEY_RESOURCE_URI -> "TEXT",
        KEY_IS_DELETED -> "boolean default 0",
        KEY_IS_NEW -> "boolean default 0")
      tempList = tempList :+ favorite_idea_table

    }

    {
      import IdeaHelper._
      val personal_idea_table = new TableDefinition(IDEA_TABLE_NAME)
      personal_idea_table insert(KEY_ID -> "TEXT PRIMARY KEY UNIQUE", KEY_OWNER -> "TEXT", KEY_RESOURCE_URI -> "TEXT", KEY_CREATED_DATE -> "TEXT",
        KEY_MODIFIED_DATE -> "TEXT", KEY_TITLE -> "TEXT not null", KEY_TEXT -> "TEXT not null",
        KEY_PARENT -> "TEXT", KEY_PUBLIC -> "TEXT", KEY_IS_IDEA_NEW -> "BOOLEAN default 0",
        KEY_IS_IDEA_EDITED -> "BOOLEAN default 0", KEY_IS_IDEA_SYNCING -> "BOOLEAN default 0",
        KEY_IS_IDEA_DELETED -> "BOOLEAN default 0")
      tempList = tempList :+ personal_idea_table
    }

    tempList
  }


  // Called when no database exists in disk and
  override def onCreate(db: SQLiteDatabase) {
    //db.execSQL(IDEA_TABLE_CREATE)
    super.onCreate(db)
  }

}
