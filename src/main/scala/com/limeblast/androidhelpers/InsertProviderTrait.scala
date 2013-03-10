package com.limeblast.androidhelpers

import android.content.{ContentValues, ContentProvider}
import android.net.Uri

trait InsertProviderTrait extends ContentProvider with BasicContentProviderTrait {

  def getInsertedId(id: Long): Uri

  def insert(uri: Uri, values: ContentValues): Uri = {
    // Open a read/write database to support the transaction
    val db = myDbHelper.getWritableDatabase

    // To add empty rows to your database by passing in an empty
    // Content Values object you must use the null column hack
    // parameter to specify the name of the column that can be
    // set to null
    val nullColumnHack: String = null

    // Insert the values into the table
    val id: Long = db.insert(table_name, nullColumnHack, values)

    // Construct and return the URI of the newly inserted row
    if(id > -1) {
      val insertedId = getInsertedId(id)
      getContext.getContentResolver.notifyChange(insertedId, null)

      insertedId
    } else {
      null
    }
  }
}
