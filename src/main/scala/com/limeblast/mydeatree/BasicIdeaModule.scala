package com.limeblast.mydeatree

import android.content.ContentValues

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 3/15/13
 * Time: 4:22 PM
 * To change this template use File | Settings | File Templates.
 */
trait BasicIdeaModule extends BasicIdeaColumns {
  def getContentValues[T <: BasicIdea](idea: T): ContentValues = {
    val newValues = new ContentValues()
    newValues.put(KEY_ID, idea.id)
    newValues.put(KEY_TITLE, idea.title)
    newValues.put(KEY_TEXT, idea.text)
    newValues.put(KEY_RESOURCE_URI, idea.resource_uri)
    newValues.put(KEY_PARENT, idea.parent)
    newValues.put(KEY_CREATED_DATE, idea.created_date)
    newValues.put(KEY_MODIFIED_DATE, idea.modified_date)

    newValues
  }
}
