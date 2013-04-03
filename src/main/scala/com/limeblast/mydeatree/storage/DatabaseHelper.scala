package com.limeblast.mydeatree.storage

import android.content.Context
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import com.limeblast.scaliteorm.{TableDefinition, DatabaseHelperTrait}
import com.limeblast.mydeatree.{PersonalResourceSync}

/**
 * Database Helper class for opening, creating and managing database version control
 * @param context Activity context
 */
class DatabaseHelper(val context: Context) extends
SQLiteOpenHelper(context, DatabaseInformation.DATABASE_NAME, null, DatabaseInformation.DATABASE_VERSION)
with DatabaseHelperTrait with PublicIdeaDatabaseModule with PersonalResourceSync {

  def tables: List[TableDefinition] = {
    var tempList: List[TableDefinition] = List()

    {
      import FavoriteIdeaColumns._
      val favorite_idea_table = new TableDefinition(TABLE_NAME)
      favorite_idea_table insert(KEY_ID -> "TEXT PRIMARY KEY UNIQUE",
        KEY_IDEA -> "TEXT not null unique",
        KEY_RESOURCE_URI -> "TEXT",
        KEY_IS_DELETED -> "boolean default 0",
        KEY_IS_NEW -> "boolean default 0",
        KEY_IS_SYNCING -> "boolean default 0")
      tempList = tempList :+ favorite_idea_table

    }

    {
      import PrivateIdeaTableInfo._
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
