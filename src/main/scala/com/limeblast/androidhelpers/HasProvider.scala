package com.limeblast.androidhelpers

import android.net.Uri
import android.content.{ContentValues, ContentResolver}

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 3/25/13
 * Time: 12:18 PM
 * To change this template use File | Settings | File Templates.
 */
trait HasProvider extends ContentValuesHelper with WhereClauseImplicitModule {
  implicit def provider_uri: Uri

  def updateObjects(resolver: ContentResolver, where: String, whereArgs: Array[String], newValues: ContentValues)(implicit uri: Uri): Int =
    resolver.update(uri, newValues, where, whereArgs)

  def updateObjects(resolver: ContentResolver, where: (String, Any), whereArgs: Array[String], newValues: ContentValues)(implicit uri: Uri): Int =
    resolver.update(uri, newValues, where, whereArgs)

  def updateObjects(resolver: ContentResolver, where: Map[String, Any], whereArgs: Array[String], newValues: ContentValues)(implicit uri: Uri): Int =
    resolver.update(uri, newValues, where, whereArgs)

  def updateObjects(resolver: ContentResolver, where: String, whereArgs: Array[String], newValues: Map[String, Any])(implicit uri: Uri): Int =
    resolver.update(uri, newValues, where, whereArgs)

  def updateObjects(resolver: ContentResolver, where: (String, Any), whereArgs: Array[String], newValues: Map[String, Any])(implicit uri: Uri): Int =
    resolver.update(uri, newValues, where, whereArgs)

  def updateObjects(resolver: ContentResolver, where: Map[String, Any], whereArgs: Array[String], newValues: Map[String, Any])(implicit uri: Uri): Int =
    resolver.update(uri, newValues, where, whereArgs)

  /**
   * Deletes objects from content provider uri
   * @param resolver Content Resolver
   * @param uri Content Uri
   * @param where Where clause
   * @param whereArgs Selection Args
   * @return Rows deleted
   */
  def deleteObjects(resolver: ContentResolver, where: String, whereArgs: Array[String])(implicit uri: Uri): Int =
    resolver.delete(uri, where, whereArgs)

  def deleteObjects(resolver: ContentResolver, where: (String, Any), whereArgs: Array[String])(implicit uri: Uri): Int =
    resolver.delete(uri, where, whereArgs)
  //deleteObjects(uri)(resolver, where, whereArgs)


  def deleteObjects(resolver: ContentResolver, where: Map[String, Any], whereArgs: Array[String])(implicit uri: Uri): Int =
    resolver.delete(uri, where, whereArgs)
  //deleteObjects(uri)(resolver, where, whereArgs)


  def insertObject(resolver: ContentResolver)(values: (String, Any)*)(implicit uri: Uri) = resolver.insert(uri, values)

}
