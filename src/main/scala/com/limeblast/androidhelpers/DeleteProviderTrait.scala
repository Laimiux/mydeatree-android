package com.limeblast.androidhelpers

import android.content.ContentProvider
import android.net.Uri

trait DeleteProviderTrait extends ContentProvider with BasicContentProviderTrait {

  def getDeleteSelect(uri: Uri): Option[String]


  def delete(uri: Uri, selection: String, selectionArgs: Array[String]): Int = {
    // open a read/write database to support the transaction
    val db = myDbHelper.getWritableDatabase

    val select = getDeleteSelect(uri) match {
      case Some(string) => string + " AND (" + selection + ")"
      case None => {
        if(selection != null)
          selection
        else
        // If no selection, delete all rows
          "1"
      }
    }

    // Perform the deletion
    val deleteCount = db.delete(table_name, select, selectionArgs)

    // Notify the observers of the change in the dataset
    getContext().getContentResolver.notifyChange(uri, null)


    deleteCount
  }
}
