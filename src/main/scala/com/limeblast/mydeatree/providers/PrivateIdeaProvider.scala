package com.limeblast.mydeatree.providers

import android.content.{ContentUris, UriMatcher, ContentValues, ContentProvider}
import android.net.Uri
import android.database.Cursor
import android.database.sqlite.{SQLiteQueryBuilder, SQLiteException, SQLiteDatabase}

import android.text.TextUtils
import android.app.SearchManager
import android.util.Log

import java.util.HashMap
import com.limeblast.mydeatree.storage.{PrivateIdeaTableInfo, DatabaseHelper}
import PrivateIdeaTableInfo._
import com.limeblast.mydeatree.{AppSettings}
import com.limeblast.mydeatree.storage.DatabaseHelper

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 12/22/12
 * Time: 12:13 PM
 * To change this template use File | Settings | File Templates.
 */
object PrivateIdeaProvider {
  val ALL_ROWS = 1
  val SINGLE_ROW = 2
  val SEARCH = 4

  val AUTHORITY = "content://com.limeblast.mydeatree.providers.IdeaContentProvider/ideas"

  val CONTENT_URI = Uri.parse(AUTHORITY)

  val uriMatcher = new UriMatcher(UriMatcher.NO_MATCH)
  uriMatcher.addURI("com.limeblast.mydeatree.providers.IdeaContentProvider", "ideas", ALL_ROWS)
  uriMatcher.addURI("com.limeblast.mydeatree.providers.IdeaContentProvider", "ideas/#", SINGLE_ROW)



  uriMatcher.addURI("com.limeblast.mydeatree.providers.IdeaContentProvider", SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH)
  uriMatcher.addURI("com.limeblast.mydeatree.providers.IdeaContentProvider", SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH)
  uriMatcher.addURI("com.limeblast.mydeatree.providers.IdeaContentProvider", SearchManager.SUGGEST_URI_PATH_SHORTCUT, SEARCH)
  uriMatcher.addURI("com.limeblast.mydeatree.providers.IdeaContentProvider", SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SEARCH)

  // Hash map to support search projections
  val SEARCH_PROJECTION_MAP: HashMap[String, String] = new HashMap()
  SEARCH_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1, KEY_TITLE + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1)
  SEARCH_PROJECTION_MAP.put(KEY_ID, KEY_ID + " AS " + "IDEA_ID")

}

class IdeaContentProvider extends ContentProvider {
  private val APP_TAG = "IDEA_CONTENT_PROVIDER"

  private var myDbHelper: DatabaseHelper = _

  def onCreate(): Boolean = {
    myDbHelper = new DatabaseHelper(getContext())
    true
  }


  def query(uri: Uri, projection: Array[String], selection: String, selectionArgs: Array[String], sortOrder: String): Cursor = {
    var db: SQLiteDatabase = null

    // Try to get writable database
    // if there is an error, get readable database
    try {
      db = myDbHelper.getWritableDatabase
    } catch {
      case e: SQLiteException => db = myDbHelper.getReadableDatabase
    }


    // Replace these with valid SQL statements if necessary
    val groupBy: String = null
    val having: String = null
    var select: String = selection

    // Use an SQLIte Query Builder to simplify constructing the
    // database query.
    val queryBuilder = new SQLiteQueryBuilder()

    // If this is a row query, limit the result set to the passed in row
    PrivateIdeaProvider.uriMatcher.`match`(uri) match {
      case PrivateIdeaProvider.SINGLE_ROW => {
        val rowID = uri.getPathSegments.get(1)
        queryBuilder.appendWhere(KEY_ID + "=" + rowID)
      }
      case PrivateIdeaProvider.SEARCH => {
        queryBuilder.appendWhere(KEY_TITLE + " LIKE \"%" +
            uri.getPathSegments.get(1) + "%\"")
        queryBuilder.setProjectionMap(PrivateIdeaProvider.SEARCH_PROJECTION_MAP)
      }

      case _ =>

    }


    // Specify the table on which to perform the query
    queryBuilder.setTables(PrivateIdeaTableInfo.TABLE_NAME)

    val cursor = queryBuilder.query(db, projection, select, selectionArgs,
      groupBy, having, sortOrder)

    // Return the result cursor
    cursor
  }

  def getType(uri: Uri): String =
    PrivateIdeaProvider.uriMatcher.`match`(uri) match {
      case PrivateIdeaProvider.ALL_ROWS => "vnd.android.cursor.dir/vnd.limeblast.ideas"
      case PrivateIdeaProvider.SINGLE_ROW => "vnd.android.cursor.item/vnd.limeblast.ideas"
      case PrivateIdeaProvider.SEARCH => SearchManager.SUGGEST_MIME_TYPE
      case _ => throw new IllegalArgumentException("Unsupported URI: " + uri)
    }


  def insert(uri: Uri, values: ContentValues): Uri = {
    // Open a read/write database to support the transaction
    val db = myDbHelper.getWritableDatabase

    // To add empty rows to your database by passing in an empty
    // Content Values object you must use the null column hack
    // parameter to specify the name of the column that can be
    // set to null
    val nullColumnHack: String = null

    // Insert the values into the table
    val id = db.insert(TABLE_NAME, nullColumnHack, values)

    // Construct and return the URI of the newly inserted row
    if (id > -1) {
      val insertedId = ContentUris.withAppendedId(PrivateIdeaProvider.CONTENT_URI, id)
      getContext.getContentResolver.notifyChange(insertedId, null)

      insertedId
    } else {
      null
    }
  }

  def delete(uri: Uri, selection: String, selectionArgs: Array[String]): Int = {
    // open a read/write database to support the transaction
    val db = myDbHelper.getWritableDatabase

    var select: String = null
    // If this is a row URI, limit the deletion to to the specified row
    PrivateIdeaProvider.uriMatcher.`match`(uri) match {
      case PrivateIdeaProvider.SINGLE_ROW => {
        val rowID = uri.getPathSegments.get(1)
        select = KEY_ID + "=" + rowID
        if (!TextUtils.isEmpty(selection)) select += " AND (" + selection + ")"
      }
      case _ => select = selection
    }

    // To return the number of deleted items you must specify a where
    // clause. TO delete all rows and return a value pass in 1
    if (selection == null) select = "1"

    // Perform the deletion
    val deleteCount = db.delete(TABLE_NAME, select, selectionArgs)

    // Notify the observers of the change in the dataset
    getContext.getContentResolver.notifyChange(uri, null)

    deleteCount
  }

  def update(uri: Uri, values: ContentValues, selection: String, selectionArgs: Array[String]): Int = {
    // open a read/write database to support the transaction
    val db = myDbHelper.getWritableDatabase

    var select: String = null
    // If this is a row URI, limit the deletion to the specified row
    PrivateIdeaProvider.uriMatcher.`match`(uri) match {
      case PrivateIdeaProvider.SINGLE_ROW => {
        val rowID = uri.getPathSegments.get(1)
        select = KEY_ID + "=" + rowID
        if (!TextUtils.isEmpty(selection)) select += " AND (" + selection + ")"
      }
      case _ => select = selection
    }

    // Perform the update
    val updateCount = db.update(TABLE_NAME, values, select, selectionArgs)

    // Notify any observers of the change in the data set
    getContext.getContentResolver.notifyChange(uri, null)
    updateCount
  }
}
