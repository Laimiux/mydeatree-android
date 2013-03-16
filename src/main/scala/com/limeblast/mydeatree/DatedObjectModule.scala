package com.limeblast.mydeatree

import android.net.Uri
import android.content.ContentResolver
import java.util

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 3/15/13
 * Time: 4:37 PM
 * To change this template use File | Settings | File Templates.
 */
trait DatedObjectModule extends BasicIdeaColumns {
  def getDatedObjects(uri: Uri, resolver: ContentResolver): util.ArrayList[DatedObject] = {
    val objects = new util.ArrayList[DatedObject]()

    val projection = Array(KEY_ID, KEY_MODIFIED_DATE)

    val cursor = resolver.query(uri, projection, null, null, null)

    val keyIdIndex = cursor.getColumnIndexOrThrow(KEY_ID)
    val keyModifiedDateIndex = cursor.getColumnIndexOrThrow(KEY_MODIFIED_DATE)

    while (cursor.moveToNext()) {
      val obj = new DatedObject(cursor.getString(keyIdIndex),
        cursor.getString(keyModifiedDateIndex))
      objects.add(obj)
    }

    cursor.close()

    objects

  }
}
