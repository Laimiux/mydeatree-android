package com.limeblast.mydeatree

import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.content.Context

import com.limeblast.scaliteorm.{TableDefinition, DatabaseHelperTrait}


object DatabaseInformation {
  // Database specific info
  val DATABASE_NAME = "mydeatree.db"
  val DATABASE_VERSION = 10

}


trait BasicIdeaColumns {
  val KEY_ID = "id"
  val KEY_TITLE = "title"
  val KEY_TEXT = "text"
  val KEY_PARENT = "parent"
  val KEY_RESOURCE_URI = "resource_uri"
  val KEY_CREATED_DATE = "created_date"
  val KEY_MODIFIED_DATE = "modified_date"
}

/**
 * Singleton object that holds important
 * variables about the database.
 */
object IdeaHelper extends BasicIdeaColumns {
  // For Idea Table
  val TABLE_NAME = "personal_ideas"

  // Personal Idea Specific Columns
  val KEY_PUBLIC = "IDEA_PUBLIC"

  // Booleans for syncing
  val KEY_IS_IDEA_NEW = "IS_IDEA_NEW"
  val KEY_IS_IDEA_EDITED = "IS_IDEA_EDITED"
  val KEY_IS_IDEA_DELETED = "IS_IDEA_DELETED"
  val KEY_IS_IDEA_SYNCING = "IS_IDEA_SYNCING"
}

/**
 * Singleton object that holds important
 * variables about the database.
 */

trait PublicIdeaDatabaseModule {

  object PublicIdeaHelper extends BasicIdeaColumns {
    // For Idea Table
    val TABLE_NAME = "public_ideas"
    // Public Idea Specific Columns
    val KEY_OWNER = "owner"
  }
}


object FavoriteIdeaColumns {
  val TABLE_NAME = "favorite_ideas"
  val KEY_ID = "id"
  val KEY_OWNER = "owner"
  val KEY_IDEA = "idea"
  val KEY_RESOURCE_URI = "resource_uri"
  val KEY_IS_DELETED = "is_deleted"
  val KEY_IS_NEW = "is_new"
  val KEY_IS_SYNCING = "is_syncing"
}

/**
 * Database Helper class for opening, creating and managing database version control
 * @param context Activity context
 */
class IdeaSQLiteHelper(val context: Context) extends
SQLiteOpenHelper(context, DatabaseInformation.DATABASE_NAME, null, DatabaseInformation.DATABASE_VERSION)
with DatabaseHelperTrait with PublicIdeaDatabaseModule with PersonalResourceSync {

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
        KEY_IS_NEW -> "boolean default 0",
        KEY_IS_SYNCING -> "boolean default 0")
      tempList = tempList :+ favorite_idea_table

    }

    {
      import IdeaHelper._
      val personal_idea_table = new TableDefinition(TABLE_NAME)
      personal_idea_table insert(KEY_ID -> "TEXT PRIMARY KEY UNIQUE",
        KEY_RESOURCE_URI -> "TEXT",
        KEY_CREATED_DATE -> "TEXT",
        KEY_MODIFIED_DATE -> "TEXT",
        KEY_TITLE -> "TEXT not null",
        KEY_TEXT -> "TEXT not null",
        KEY_PARENT -> "TEXT",
        KEY_PUBLIC -> "TEXT",
        KEY_IS_IDEA_NEW -> "BOOLEAN default 0",
        KEY_IS_IDEA_EDITED -> "BOOLEAN default 0",
        KEY_IS_IDEA_SYNCING -> "BOOLEAN default 0",
        KEY_IS_IDEA_DELETED -> "BOOLEAN default 0")
      tempList = tempList :+ personal_idea_table
    }

    {
      import PublicIdeaHelper._
      val public_idea_table = new TableDefinition(TABLE_NAME)
      public_idea_table insert(KEY_ID -> "TEXT PRIMARY KEY UNIQUE",
        KEY_OWNER -> "TEXT",
        KEY_RESOURCE_URI -> "TEXT",
        KEY_CREATED_DATE -> "TEXT",
        KEY_MODIFIED_DATE -> "TEXT",
        KEY_TITLE -> "TEXT not null",
        KEY_TEXT -> "TEXT not null",
        KEY_PARENT -> "TEXT")
      tempList = tempList :+ public_idea_table
    }

    tempList
  }


  // Called when no database exists in disk and
  override def onCreate(db: SQLiteDatabase) {
    //db.execSQL(IDEA_TABLE_CREATE)
    super.onCreate(db)
  }

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    super.onUpgrade(db, oldVersion, newVersion)

    startSyncingAllPersonalResources()
  }
}
