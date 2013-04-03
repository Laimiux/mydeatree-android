package com.limeblast.mydeatree.providers

import android.content.{ContentUris, UriMatcher, ContentProvider}
import android.net.Uri
import android.database.sqlite.SQLiteQueryBuilder
import com.limeblast.androidhelpers.{InsertProviderTrait, QueryProviderTrait, UpdateProviderTrait, DeleteProviderTrait}
import com.limeblast.mydeatree.storage.{FavoriteIdeaColumns, DatabaseHelper}

object FavoriteIdeaProvider {
  val ALL_ROWS = 1
  val SINGLE_ROW = 2

  val CONTENT_URI = Uri.parse("content://com.limeblast.mydeatree.providers.FavoriteIdeaProvider/favorite_ideas")

  val uriMatcher = new UriMatcher(UriMatcher.NO_MATCH)
  uriMatcher.addURI("com.limeblast.mydeatree.providers.FavoriteIdeaProvider", "favorite_ideas", ALL_ROWS)
  uriMatcher.addURI("com.limeblast.mydeatree.providers.FavoriteIdeaProvider", "favorite_ideas/#", SINGLE_ROW)

}


class FavoriteIdeaProvider extends ContentProvider with DeleteProviderTrait
with UpdateProviderTrait with QueryProviderTrait with InsertProviderTrait {

  lazy val myDbHelper = new DatabaseHelper(getContext())

  val table_name: String = FavoriteIdeaColumns.TABLE_NAME


  def getDeleteSelect(uri: Uri): Option[String] = {
    FavoriteIdeaProvider.uriMatcher.`match`(uri) match {
      case FavoriteIdeaProvider.SINGLE_ROW => Some(FavoriteIdeaColumns.KEY_ID + "=" + uri.getPathSegments.get(1))
      case _ => None
    }
  }


  def getUpdateSelect(uri: Uri): Option[String] = getDeleteSelect(uri)


  override def setUpQueryBuilder(uri: Uri, builder: SQLiteQueryBuilder) {
    FavoriteIdeaProvider.uriMatcher.`match`(uri) match {
      case FavoriteIdeaProvider.SINGLE_ROW => builder.appendWhere(FavoriteIdeaColumns.KEY_ID + "=" + uri.getPathSegments.get(1))
      case _ =>
    }
  }


  def getInsertedId(id: Long): Uri = ContentUris.withAppendedId(FavoriteIdeaProvider.CONTENT_URI, id)

  def onCreate(): Boolean = {
    myDbHelper
    true
  }

  def getType(uri: Uri): String = {
    FavoriteIdeaProvider.uriMatcher.`match`(uri) match {
      case FavoriteIdeaProvider.ALL_ROWS => "vnd.android.cursor.dir/vnd.limeblast.favorite_ideas"
      case FavoriteIdeaProvider.SINGLE_ROW => "vnd.android.cursor.item/vnd.limeblast.favorite_ideas"
      case _ => throw new IllegalArgumentException("Unsupported URI: " + uri)
    }
  }


}
