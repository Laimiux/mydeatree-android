package com.limeblast.androidhelpers

import android.content.ContentProvider
import android.database.sqlite.{SQLiteQueryBuilder, SQLiteException}
import android.net.Uri
import android.database.Cursor

trait QueryProviderTrait extends ContentProvider with BasicContentProviderTrait {

  def getQuerySelect(uri: Uri): Option[String] = None

  def setUpQueryBuilder(uri: Uri, builder: SQLiteQueryBuilder) = {}


  def getQueryGroupBy: Option[String] = None
  def getQueryHaving: Option[String] = None

  def query(uri: Uri, projection: Array[String], selection: String, selectionArgs: Array[String], sortOrder: String): Cursor = {
    val db = try {
      myDbHelper.getWritableDatabase
    } catch {
      case e: SQLiteException => myDbHelper.getReadableDatabase
    }

    val groupBy = getQueryGroupBy match {
      case Some(clause) => clause
      case _ => null
    }
    val having = getQueryHaving match {
      case Some(clause) => clause
      case _ => null
    }

    val select = getQuerySelect(uri) match {
      case Some(string) => string + " AND (" + selection + ")"
      case _ => selection
    }

    // Use an SQLIte Query Builder to simplify constructing the
    // database query.
    val queryBuilder = new SQLiteQueryBuilder()

    // Gives users a callback ability to modify the query builder
    setUpQueryBuilder(uri, queryBuilder)

    queryBuilder.setTables(table_name)


    queryBuilder.query(db, projection, select, selectionArgs, groupBy, having, sortOrder)
  }
}
