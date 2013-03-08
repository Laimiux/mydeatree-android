package com.limeblast.scaliteorm

import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}


trait DatabaseHelperTrait extends SQLiteOpenHelper {
  def tables: Option[Map[String, TableDefinition]]

  def onCreate(db: SQLiteDatabase) {

  }
}
