package com.limeblast.mydeatree

import android.database.Cursor

import java.util
import storage.PrivateIdeaTableInfo

trait PersonalIdeaGetModule {
  class Columns(val cursor: Cursor) {
    import PrivateIdeaTableInfo._


    // get all columns
    val keyTitleIndex: Int = cursor.getColumnIndexOrThrow(KEY_TITLE)
    val keyTextIndex = cursor.getColumnIndexOrThrow(KEY_TEXT)
    val keyResourceUriIndex = cursor.getColumnIndexOrThrow(KEY_RESOURCE_URI)
    val keyModifiedDateIndex = cursor.getColumnIndexOrThrow(KEY_MODIFIED_DATE)
    val keyCreatedDateIndex = cursor.getColumnIndexOrThrow(KEY_CREATED_DATE)
    val keyParentIndex = cursor.getColumnIndexOrThrow(KEY_PARENT)
    val keyIdIndex = cursor.getColumnIndexOrThrow(KEY_ID)
    val keyPublicIndex = cursor.getColumnIndexOrThrow(KEY_PUBLIC)


  }

  /**
   * Retrieves an idea, given its column keys and a cursor
   * Will fail, if cursor didn't moveNext.
   */
  private def getIdeaFromCursor(columns: Columns, cursor: Cursor): Idea = new Idea(cursor.getString(columns.keyTitleIndex),
    cursor.getString(columns.keyTextIndex),
    cursor.getString(columns.keyIdIndex),
    cursor.getString(columns.keyParentIndex),
    cursor.getString(columns.keyCreatedDateIndex),
    cursor.getString(columns.keyModifiedDateIndex),
    cursor.getString(columns.keyResourceUriIndex),
    cursor.getInt(columns.keyPublicIndex) > 0)




  def getIdeaFromCursor(cursor: Cursor): Option[Idea] = {

    val columns = new Columns(cursor)


    val ideaOption  = if (cursor.moveToNext()) {
        val idea = getIdeaFromCursor(columns, cursor)
        // Log.d(APP_TAG, "Private idea parent is " + idea.parent)
        Some(idea)
      } else {
        None
      }



    cursor.close()

    ideaOption
  }

  def getIdeasFromCursor(cursor: Cursor): util.ArrayList[Idea] = {
    val ideas = new util.ArrayList[Idea]()

    val columns = new Columns(cursor)

    while (cursor.moveToNext()) {
      val idea = getIdeaFromCursor(columns, cursor)

      // Log.d(APP_TAG, "Private idea parent is " + idea.parent)
      ideas.add(0, idea)
    }

    cursor.close()

    ideas
  }
}

object PersonalIdeaAccessor extends PersonalIdeaGetModule
