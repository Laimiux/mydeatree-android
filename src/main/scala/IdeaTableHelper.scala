package com.limeblast.mydeatree

import android.content.{Context, ContentResolver, ContentValues}
import AppSettings._

import java.util
import android.net.Uri
import android.database.Cursor
import java.lang.reflect.Field


import android.util.Log

object IdeaTableHelper {

  private def createBasicIdea[T <: BasicIdea](idea: T): ContentValues = {
    val newValues = new ContentValues()
    newValues.put(IdeaHelper.KEY_ID, idea.id)
    newValues.put(IdeaHelper.KEY_TITLE, idea.title)
    newValues.put(IdeaHelper.KEY_TEXT, idea.text)
    newValues.put(IdeaHelper.KEY_CREATED_DATE, idea.created_date)
    newValues.put(IdeaHelper.KEY_MODIFIED_DATE, idea.modified_date)

    newValues
  }
  /*
   * Create new Content Values for idea object
   */
  def createNewIdeaValues(idea: Idea): ContentValues = {
    // Create new values
    val newValues = createBasicIdea(idea)
    newValues.put(IdeaHelper.KEY_OWNER, USERNAME)
    newValues.put(IdeaHelper.KEY_PARENT, idea.parent)
    newValues.put(IdeaHelper.KEY_RESOURCE_URI, idea.resource_uri)
    newValues.put(IdeaHelper.KEY_PUBLIC, idea.public)

    newValues
  }

  def createNewIdeaValues(idea: PublicIdea): ContentValues = {
    val newValues = createBasicIdea(idea)
    newValues.put(IdeaHelper.KEY_OWNER, idea.owner.username)
    newValues.put(IdeaHelper.KEY_PARENT, idea.parent)
    newValues.put(IdeaHelper.KEY_RESOURCE_URI, idea.resource_uri)
    newValues.put(IdeaHelper.KEY_PUBLIC, true)

    newValues
  }


  def getSavedObjects(uri: Uri, resolver: ContentResolver): util.ArrayList[ObjectIdWithDate] = {
    val objects = new util.ArrayList[ObjectIdWithDate]()

    val projection = Array(IdeaHelper.KEY_ID, IdeaHelper.KEY_MODIFIED_DATE)

    val cursor = resolver.query(uri, projection, null, null, null)

    val keyIdIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_ID)
    val keyModifiedDateIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_MODIFIED_DATE)

    while (cursor.moveToNext()) {
      val obj = new ObjectIdWithDate(cursor.getString(keyIdIndex),
        cursor.getString(keyModifiedDateIndex))
      objects.add(obj)
    }

    cursor.close()

    objects
  }

  def getIdeas(cursor: Cursor): util.ArrayList[Idea] = {
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


  def retrieveObject(context: Context, uri: Uri, cls: Class[_]) {
    val superclass = cls.getSuperclass
    val fields: Array[Field] = cls.getDeclaredFields
    val superfields: Array[Field] = superclass.getDeclaredFields


    val resolver = context.getContentResolver()


    val cursor = resolver.query(uri, null, null, null, null)

    val keys = new Array[Int](fields.length)

    var i = 0
    for (field <- fields) {
      Log.d(APP_TAG, "Field " + i + " = " + field.toString)
      //keys(i) = cursor.getColumnIndexOrThrow(field.toString)
      i += 1
    }

    for (key <- keys){

    }


    cursor.close()

  }

}
