package com.limeblast.androidhelpers

import android.content.ContentResolver

import com.limeblast.mydeatree.{RESTfulProvider, IdeaHelper, Idea}
import android.net.Uri
import android.util.Log

object ProviderHelper {

  def makeWhereClause(tuple: (String, Any)): String =
    tuple match {
      case (one: String, two: Int) => one + "=" + two
      case (one: String, two: String) => one + "='" + two + "'"
      case (one: String, two: Boolean) => {
        two match {
          case true => one + "=" + 1
          case false => one + "=" + 0
        }

      }
    }

  def makeWhereClause(whereArgs: Map[String, Any]): String =
    (whereArgs :\ "")((tuple, where) =>
      where match {
        case empty: String if (empty.equals("")) => makeWhereClause(tuple)
        case _ => where + " AND " + makeWhereClause(tuple)
      })


  /**
   * Deletes objects from content provider uri
   * @param resolver Content Resolver
   * @param uri Content Uri
   * @param where Where clause
   * @param whereArgs Selection Args
   * @return Rows deleted
   */
  def deleteObjects(resolver: ContentResolver, uri: Uri, where: String, whereArgs: Array[String]): Int =
    resolver.delete(uri, where, whereArgs)

  def deleteObjects(resolver: ContentResolver, uri: Uri, where: (String, Any), whereArgs: Array[String]): Int =
    deleteObjects(resolver, uri, makeWhereClause(where), whereArgs)


  def deleteObjects(resolver: ContentResolver, uri: Uri, where: Map[String, Any], whereArgs: Array[String]): Int =
    deleteObjects(resolver, uri, makeWhereClause(where), whereArgs)

}

trait Providable[T] {

  def contentResolver: ContentResolver

  def object_list_uri: Uri

  def object_uri: Option[String] // should use id to construct it

  val object_type: Class[_]

  def column_names: Array[String]


  def get_all(): Any = {

    val cursor = contentResolver.query(object_list_uri, null, null, null, null)

    val columns = (column_names :\ Array[Int]())((name, arr) => {
      arr :+ cursor.getColumnIndexOrThrow(name)
    })

    while (cursor.moveToNext) {

    }

    null
  }

}

/* Move this function eventually */
/*
private def getIdea(resource_uri: String): Idea = {
  val cr = getActivity.getContentResolver

  val selection = IdeaHelper.KEY_RESOURCE_URI + "='" + resource_uri + "'"

  val cursor = cr.query(RESTfulProvider.CONTENT_URI, null, selection, null, null)

  val keyTitleIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_TITLE)
  val keyTextIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_TEXT)
  val keyResourceUriIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_RESOURCE_URI)
  val keyModifiedDateIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_MODIFIED_DATE)
  val keyCreatedDateIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_CREATED_DATE)
  val keyParentIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_PARENT)
  val keyIdIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_ID)
  //val keyOwnerIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_OWNER)
  val keyPublicIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_PUBLIC)


  if (AppSettings.DEBUG) Log.d(APP_TAG, cursor.getColumnNames.toString)
  var idea: Idea = null

  // Should loop only once
  while (cursor.moveToNext()) {
    idea = new Idea(cursor.getString(keyTitleIndex),
      cursor.getString(keyTextIndex),
      cursor.getString(keyIdIndex),
      cursor.getString(keyParentIndex),
      cursor.getString(keyCreatedDateIndex),
      cursor.getString(keyModifiedDateIndex),
      cursor.getString(keyResourceUriIndex),
      cursor.getInt(keyPublicIndex) > 0)
  }

  cursor.close()
  idea
}
*/


class PersonalIdeaProvider(val contentResolver: ContentResolver) extends Providable[Idea] {

  val object_type: Class[Idea] = classOf[Idea]

  def object_list_uri: Uri = RESTfulProvider.CONTENT_URI

  def object_uri: Option[String] = None

  def column_names: Array[String] = Array(IdeaHelper.KEY_ID, IdeaHelper.KEY_TITLE)
}