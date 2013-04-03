package com.limeblast.mydeatree.storage

import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.content.Context

import com.limeblast.scaliteorm.{TableDefinition, DatabaseHelperTrait}





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
object PrivateIdeaTableInfo extends BasicIdeaColumns {
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




object FavoriteIdeaColumns {
  val TABLE_NAME = "favorite_ideas"
  val KEY_ID = "id"
  val KEY_IDEA = "idea"
  val KEY_RESOURCE_URI = "resource_uri"
  val KEY_IS_DELETED = "is_deleted"
  val KEY_IS_NEW = "is_new"
  val KEY_IS_SYNCING = "is_syncing"
}
