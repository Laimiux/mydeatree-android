package com.limeblast.androidhelpers

import android.content.{ContentValues, ContentResolver}

import com.limeblast.mydeatree.{IdeaHelper, Idea}
import android.net.Uri
import com.limeblast.mydeatree.providers.RESTfulProvider


trait ProviderAccessModule extends WhereClauseImplicitModule {

  @deprecated object ProviderHelper extends ContentValuesHelper {
    /**
     * Updates objects in the database
     * @param resolver Content Resolver
     * @param uri Object uri
     * @param where Where clause
     * @param whereArgs Where arguments
     * @param newValues New values for the objects
     * @return Number of objects updated
     */
    def updateObjects(resolver: ContentResolver, uri: Uri, where: String, whereArgs: Array[String], newValues: ContentValues): Int =
      resolver.update(uri, newValues, where, whereArgs)

    def updateObjects(resolver: ContentResolver, uri: Uri, where: (String, Any), whereArgs: Array[String], newValues: ContentValues): Int =
      resolver.update(uri, newValues, where, whereArgs)

    def updateObjects(resolver: ContentResolver, uri: Uri, where: Map[String, Any], whereArgs: Array[String], newValues: ContentValues): Int =
      resolver.update(uri, newValues, where, whereArgs)

    def updateObjects(resolver: ContentResolver, uri: Uri, where: String, whereArgs: Array[String], newValues: Map[String, Any]): Int =
      resolver.update(uri, newValues, where, whereArgs)

    def updateObjects(resolver: ContentResolver, uri: Uri, where: (String, Any), whereArgs: Array[String], newValues: Map[String, Any]): Int =
      resolver.update(uri, newValues, where, whereArgs)

    def updateObjects(resolver: ContentResolver, uri: Uri, where: Map[String, Any], whereArgs: Array[String], newValues: Map[String, Any]): Int =
      resolver.update(uri, newValues, where, whereArgs)

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
      resolver.delete(uri, where, whereArgs)


    def deleteObjects(resolver: ContentResolver, uri: Uri, where: Map[String, Any], whereArgs: Array[String]): Int =
      resolver.delete(uri, where, whereArgs)


    def insertObject(uri: Uri)(resolver: ContentResolver)(values: (String, Any)*) =
      resolver.insert(uri, values)

  }
}



