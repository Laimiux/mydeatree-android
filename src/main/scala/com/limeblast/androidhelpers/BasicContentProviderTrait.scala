package com.limeblast.androidhelpers

import android.database.sqlite.SQLiteOpenHelper


trait BasicContentProviderTrait {
  def table_name: String
  def myDbHelper: SQLiteOpenHelper
}
