package com.limeblast.mydeatree

import android.database.Cursor

import java.util

trait PersonalIdeaGetModule {
  def getIdeasFromCursor(cursor: Cursor): util.ArrayList[Idea] = {
    val ideas = new util.ArrayList[Idea]()

    val keyTitleIndex: Int = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_TITLE)
    val keyTextIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_TEXT)
    val keyResourceUriIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_RESOURCE_URI)
    val keyModifiedDateIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_MODIFIED_DATE)
    val keyCreatedDateIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_CREATED_DATE)
    val keyParentIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_PARENT)
    val keyIdIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_ID)
    val keyPublicIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_PUBLIC)

    while (cursor.moveToNext()) {
      val idea = new Idea(cursor.getString(keyTitleIndex),
        cursor.getString(keyTextIndex),
        cursor.getString(keyIdIndex),
        cursor.getString(keyParentIndex),
        cursor.getString(keyCreatedDateIndex),
        cursor.getString(keyModifiedDateIndex),
        cursor.getString(keyResourceUriIndex),
        cursor.getInt(keyPublicIndex) > 0)


      // Log.d(APP_TAG, "Private idea parent is " + idea.parent)
      ideas.add(0, idea)
    }

    cursor.close()

    ideas
  }
}
