package com.limeblast.mydeatree.fragments

import com.actionbarsherlock.app.{SherlockListFragment}
import android.view.{View, ViewGroup, LayoutInflater}
import android.os.{Bundle}
import com.limeblast.mydeatree._
import adapters.{FavoriteIdeaListAdapter}
import android.util.Log
import com.limeblast.mydeatree.AppSettings._
import android.support.v4.app.LoaderManager
import android.database.Cursor
import android.support.v4.content.{CursorLoader, Loader}

import providers.{FavoriteIdeaProvider, PublicIdeaProvider}

import java.util
import com.limeblast.androidhelpers.{ScalaHandler, WhereClauseModule}
import android.widget.TextView
import services.FavoriteIdeaGetService
import android.content.Intent

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 3/15/13
 * Time: 12:32 PM
 * To change this template use File | Settings | File Templates.
 */
class FavoriteIdeaFragment extends SherlockListFragment with LoaderManager.LoaderCallbacks[Cursor] with PublicIdeaDatabaseModule with WhereClauseModule {

  private var favoriteIdeas = new util.ArrayList[PublicIdea]()
  private lazy val arrayAdapter = new FavoriteIdeaListAdapter(getActivity(), R.layout.public_idea_entry, favoriteIdeas)
  private var handler: ScalaHandler = _

  private var noIdeasTextView: TextView = _


  //-------------------------------------------------------\\
  //------------ FRAGMENT LIFECYCLE EVENTS ----------------\\
  //-------------------------------------------------------\\
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    //if (AppSettings.DEBUG) Log.d(APP_TAG, "Creating PrivateIdeaListFragment view")
    val fragmentView = inflater.inflate(R.layout.favorite_idea_layout, container, false)

    fragmentView.setFocusableInTouchMode(true)
    fragmentView.requestFocus()

    noIdeasTextView = fragmentView.findViewById(R.id.no_ideas_text_view).asInstanceOf[TextView]

    fragmentView
  }


  override def onActivityCreated(savedInstanceState: Bundle) {
    super.onActivityCreated(savedInstanceState)

    handler = new ScalaHandler()

    setListAdapter(arrayAdapter)

    if (!Helpers.isServiceRunning(classOf[FavoriteIdeaGetService].getName, getActivity))
      refreshIdeas()
  }

  override def onResume() {
    refresh()
    super.onResume()
  }

  def refresh() {
    if (getActivity != null) {
      getLoaderManager.restartLoader(0, null, FavoriteIdeaFragment.this)
    }
  }

  def refreshIdeas() {

    val intent = new Intent(getActivity.getApplicationContext, classOf[FavoriteIdeaGetService])
    // Start service
    getActivity.startService(intent)
  }


  /**
   * Checks if public idea is in favorite ideas
   * @param uri Idea resource_uri
   * @return True if idea is a favorite
   */
  private def isIdeaInFavorites(uri: String): Boolean = {
    val resolver = getActivity.getContentResolver
    val select = makeWhereClause((FavoriteIdeaColumns.KEY_IDEA, uri), (FavoriteIdeaColumns.KEY_IS_DELETED, false))

    val cursor = App.FavoriteIdeaResource.Provider.getObjects(resolver, Array(), select, null, null)

    val doesIdeaExist = cursor.getCount() > 0

    cursor.close()

    doesIdeaExist
  }

  //-------------------------------------------------------\\
  //--------- LOADER MANAGER CALLBACK METHODS -------------\\
  //-------------------------------------------------------\\
  def onCreateLoader(p1: Int, p2: Bundle): Loader[Cursor] = {
    /*
    val select =

      if (parent_idea != null)
        PublicIdeaHelper.KEY_PARENT + "='" + parent_idea + "'"
      else
        PublicIdeaHelper.KEY_PARENT + " IS NULL"
        */


    new CursorLoader(getActivity.getApplicationContext, PublicIdeaProvider.CONTENT_URI, null, null, null, null)
  }

  def onLoadFinished(loader: Loader[Cursor], cursor: Cursor) {
    val keyTitleIndex = cursor.getColumnIndexOrThrow(PublicIdeaHelper.KEY_TITLE)
    val keyTextIndex = cursor.getColumnIndexOrThrow(PublicIdeaHelper.KEY_TEXT)
    val keyResourceUriIndex = cursor.getColumnIndexOrThrow(PublicIdeaHelper.KEY_RESOURCE_URI)
    val keyModifiedDateIndex = cursor.getColumnIndexOrThrow(PublicIdeaHelper.KEY_MODIFIED_DATE)
    val keyCreatedDateIndex = cursor.getColumnIndexOrThrow(PublicIdeaHelper.KEY_CREATED_DATE)
    val keyIdIndex = cursor.getColumnIndexOrThrow(PublicIdeaHelper.KEY_ID)
    val keyOwnerIndex = cursor.getColumnIndexOrThrow(PublicIdeaHelper.KEY_OWNER)
    val keyParentIndex = cursor.getColumnIndexOrThrow(PublicIdeaHelper.KEY_PARENT)


    favoriteIdeas.clear()


    while (cursor.moveToNext()) {
      // check if the public idea is in favorites!
      val resource_uri = cursor.getString(keyResourceUriIndex)

      if (isIdeaInFavorites(resource_uri)) {
        val idea = new PublicIdea(cursor.getString(keyTitleIndex),
          cursor.getString(keyTextIndex),
          cursor.getString(keyIdIndex),
          cursor.getString(keyParentIndex),
          cursor.getString(keyCreatedDateIndex),
          cursor.getString(keyModifiedDateIndex),
          resource_uri,
          new Owner(cursor.getString(keyOwnerIndex)))



        favoriteIdeas.add(0, idea)
      }
    }


    handler.post(arrayAdapter.notifyDataSetChanged())


    if (favoriteIdeas.size() > 0) {
      handler.post(noIdeasTextView.setVisibility(View.INVISIBLE))
    } else {
      handler.post(noIdeasTextView.setVisibility(View.VISIBLE))
    }


    Log.d(APP_TAG, "There are " + favoriteIdeas.size() + " favorite ideas.")
  }

  def onLoaderReset(p1: Loader[Cursor]) {}

  //-------------------------------------------------------\\
  //------ END OF LOADER MANAGER CALLBACK METHODS ---------\\
  //-------------------------------------------------------\\

}
