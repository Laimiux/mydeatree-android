package com.limeblast.androidhelpers

import android.net.Uri
import android.content.{ContentValues, ContentResolver}
import java.sql.DriverManager

trait HasProvider extends ContentValuesHelper with WhereClauseImplicitModule {
  def provider_uri: Uri


  def getObjects(resolver: ContentResolver, projection: Array[String], selection: String, selectionArgs: Array[String], sortOrder: String) =
    resolver.query(provider_uri, projection, selection, selectionArgs, sortOrder)

  def updateObjects(resolver: ContentResolver, where: String, whereArgs: Array[String], newValues: ContentValues): Int =
    resolver.update(provider_uri, newValues, where, whereArgs)

  def updateObjects(resolver: ContentResolver, where: (String, Any), whereArgs: Array[String], newValues: ContentValues): Int =
    resolver.update(provider_uri, newValues, where, whereArgs)

  def updateObjects(resolver: ContentResolver, where: Map[String, Any], whereArgs: Array[String], newValues: ContentValues): Int =
    resolver.update(provider_uri, newValues, where, whereArgs)

  def updateObjects(resolver: ContentResolver, where: String, whereArgs: Array[String], newValues: Map[String, Any]): Int =
    resolver.update(provider_uri, newValues, where, whereArgs)

  def updateObjects(resolver: ContentResolver, where: (String, Any), whereArgs: Array[String], newValues: Map[String, Any]): Int =
    resolver.update(provider_uri, newValues, where, whereArgs)

  def updateObjects(resolver: ContentResolver, where: Map[String, Any], whereArgs: Array[String], newValues: Map[String, Any]): Int =
    resolver.update(provider_uri, newValues, where, whereArgs)

  /**
   * Deletes objects from content provider uri
   * @param resolver Content Resolver
   * @param where Where clause
   * @param whereArgs Selection Args
   * @return Rows deleted
   */
  def deleteObjects(resolver: ContentResolver, where: String, whereArgs: Array[String]): Int =
    resolver.delete(provider_uri, where, whereArgs)

  def deleteObjects(resolver: ContentResolver, where: (String, Any), whereArgs: Array[String]): Int =
    resolver.delete(provider_uri, where, whereArgs)


  def deleteObjects(resolver: ContentResolver, where: Map[String, Any], whereArgs: Array[String]): Int =
    resolver.delete(provider_uri, where, whereArgs)

  def insertObject(resolver: ContentResolver)(values: (String, Any)*) = resolver.insert(provider_uri, values)

}
