package com.limeblast.androidhelpers

import android.content.{ContentValues, ContentProvider}
import android.net.Uri

trait UpdateProviderTrait extends ContentProvider with BasicContentProviderTrait {
  def getUpdateSelect(uri: Uri): Option[String]

  def update(uri: Uri, values: ContentValues, selection: String, selectionArgs: Array[String]): Int = {
    val db = myDbHelper.getWritableDatabase

    val select = getUpdateSelect(uri) match {
      case Some(string) => string + " AND (" + selection + ")"
      case None => selection
    }

    // Perform the update
    val updateCount = db.update(table_name, values, select, selectionArgs)

    // Notify any observers of the change in the data set
    getContext.getContentResolver.notifyChange(uri, null)

    updateCount
  }
}
