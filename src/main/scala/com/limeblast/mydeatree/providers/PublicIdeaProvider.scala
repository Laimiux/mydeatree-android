package com.limeblast.mydeatree.providers

import android.content.{ContentUris, UriMatcher, ContentProvider}
import com.limeblast.androidhelpers.{InsertProviderTrait, QueryProviderTrait, UpdateProviderTrait, DeleteProviderTrait}
import com.limeblast.mydeatree.{PublicIdeaDatabaseModule, IdeaSQLiteHelper}
import android.database.sqlite.{SQLiteQueryBuilder, SQLiteOpenHelper}
import android.net.Uri


object PublicIdeaProvider {
  val ALL_ROWS = 1
  val SINGLE_ROW = 2

  val CONTENT_URI = Uri.parse("content://com.limeblast.mydeatree.providers.PublicIdeaProvider/public_ideas")

  val uriMatcher = new UriMatcher(UriMatcher.NO_MATCH)
  uriMatcher.addURI("com.limeblast.mydeatree.providers.PublicIdeaProvider", "public_ideas", ALL_ROWS)
  uriMatcher.addURI("com.limeblast.mydeatree.providers.PublicIdeaProvider", "public_ideas/#", SINGLE_ROW)

}

class PublicIdeaProvider extends ContentProvider with DeleteProviderTrait
with UpdateProviderTrait with QueryProviderTrait with InsertProviderTrait with PublicIdeaDatabaseModule {


  val table_name: String = PublicIdeaHelper.TABLE_NAME
  var myDbHelper: SQLiteOpenHelper = _

  def onCreate(): Boolean = {
    myDbHelper = new IdeaSQLiteHelper(getContext())
    true
  }


  override def setUpQueryBuilder(uri: Uri, builder: SQLiteQueryBuilder) {
    PublicIdeaProvider.uriMatcher.`match`(uri) match {
      case PublicIdeaProvider.SINGLE_ROW => builder.appendWhere(PublicIdeaHelper.KEY_ID + "=" + uri.getPathSegments.get(1))
      case _ =>
    }
  }


  def getDeleteSelect(uri: Uri): Option[String] = {
    PublicIdeaProvider.uriMatcher.`match`(uri) match {
      case PublicIdeaProvider.SINGLE_ROW => Some(PublicIdeaHelper.KEY_ID + "=" + uri.getPathSegments.get(1))
      case _ => None
    }
  }

  def getUpdateSelect(uri: Uri): Option[String] = getDeleteSelect(uri)

  def getInsertedId(id: Long): Uri =  ContentUris.withAppendedId(PublicIdeaProvider.CONTENT_URI, id)


  def getType(uri: Uri): String = {
    PublicIdeaProvider.uriMatcher.`match`(uri) match {
      case PublicIdeaProvider.ALL_ROWS => "vnd.android.cursor.dir/vnd.limeblast.public_ideas"
      case PublicIdeaProvider.SINGLE_ROW => "vnd.android.cursor.item/vnd.limeblast.public_ideas"
      case _ => throw new IllegalArgumentException("Unsupported URI: " + uri)
    }
  }

}
