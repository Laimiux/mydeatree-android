package com.limeblast.mydeatree.fragments

import com.actionbarsherlock.app.{SherlockListFragment}
import android.view.{View, ViewGroup, LayoutInflater}
import android.os.{Handler, Bundle}
import com.limeblast.mydeatree._
import activities.MainActivity
import adapters.{FavoriteIdeaListAdapter}
import android.util.Log
import com.limeblast.mydeatree.AppSettings._
import android.support.v4.app.LoaderManager
import android.database.Cursor
import android.support.v4.content.{CursorLoader, Loader}

import providers.{FavoriteIdeaProvider, PublicIdeaProvider}

import java.util
import com.limeblast.androidhelpers.{ScalifiedTraitModule, WhereClauseModule}
import android.widget.TextView
import services.{FavoriteIdeaDeleteService, FavoriteIdeaPostService, FavoriteIdeaGetService}
import android.content.Intent
import concurrent.ops._
import com.limeblast.rest.JsonModule

import android.app.Activity
import storage.{FavoriteIdeaColumns, PublicIdeaDatabaseModule}

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 3/15/13
 * Time: 12:32 PM
 * To change this template use File | Settings | File Templates.
 */
class FavoriteIdeaFragment extends SherlockListFragment with LoaderManager.LoaderCallbacks[Cursor]
with PublicIdeaDatabaseModule with WhereClauseModule with JsonModule
with FavoriteIdeaProviderModule with ScalifiedTraitModule {

  private var favoriteIdeas = new util.ArrayList[PublicIdea]()
  private lazy val arrayAdapter = new FavoriteIdeaListAdapter(getActivity(), R.layout.public_idea_entry, favoriteIdeas)
  private var handler: Handler = _

  private var noIdeasTextView: TextView = _


  //-------------------------------------------------------\\
  //------------ FRAGMENT LIFECYCLE EVENTS ----------------\\
  //-------------------------------------------------------\\
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val fragmentView = inflater.inflate(R.layout.favorite_idea_layout, container, false)

    fragmentView.setFocusableInTouchMode(true)
    fragmentView.requestFocus()

    noIdeasTextView = fragmentView.findViewById(R.id.no_ideas_text_view).asInstanceOf[TextView]

    fragmentView
  }


  override def onActivityCreated(savedInstanceState: Bundle) {
    super.onActivityCreated(savedInstanceState)

    handler = new Handler()

    setListAdapter(arrayAdapter)

  }

  override def onResume() {
    refresh()

    // Refresh resources
    spawn {
      refreshResources()
    }

    super.onResume()
  }

  def refresh() {
    if (getActivity != null) {
      getLoaderManager.restartLoader(0, null, FavoriteIdeaFragment.this)
    }
  }

  def getLatestFavorites[F](success: => F) {

    val intent = new Intent(getActivity.getApplicationContext, classOf[FavoriteIdeaGetService])

    intent.putExtra(App.FAVORITE_IDEA_GET_RESULT_RECEIVER, (resultCode: Int, resultData: Bundle) => {
      if (resultCode == 0) {
        if (App.DEBUG) Log.d("FavoriteIdeaFragment", "Favorite Idea Get Service was successful.")
        success
      }
    })
    // Start service
    getActivity.startService(intent)
  }


  /**
   * Checks if public idea is in favorite ideas
   * @param uri Idea resource_uri
   * @return True if idea is a favorite
   */
  private def isIdeaInFavorites(uri: String): Boolean = getResolver() match {
      case None => {
        if (App.DEBUG) Log.d("FavoriteIdeaFragment", "isIdeaInFavorites method couldn't retrieve ContentResolver")
        false
      }
      case Some(resolver) => {
        val select = makeWhereClause((FavoriteIdeaColumns.KEY_IDEA, uri), (FavoriteIdeaColumns.KEY_IS_DELETED, false))

        val cursor = getObjects(resolver, Array(), select, null, null)

        val doesIdeaExist = cursor.getCount() > 0

        cursor.close()

        doesIdeaExist
      }
    }






  //-------------------------------------------------------\\
  //------------------ SYNC FUNCTIONS ---------------------\\
  //-------------------------------------------------------\\
  private def refreshResources() {
    if (App.DEBUG) Log.d("MainActivity", "---------- STARTING REFRESH RESOURCES ------------")

    if (isOnline(getActivity) && !isServiceRunning(classOf[FavoriteIdeaGetService].getName)(getActivity)) {

      getLatestFavorites({
        // Refresh favorite objects
        val favoritesToUpload = getFavoritesToUpload()
        val favoritesToDelete = getFavoritesToDelete()

        if (App.DEBUG) {
          Log.d("MainActivity", "There is " + favoritesToUpload.size + " favorites to upload")
          Log.d("MainActivity", "There is " + favoritesToDelete.size + " favorites to delete")
        }

        for (fav <- favoritesToUpload) {
          val intent = new Intent(getActivity, classOf[FavoriteIdeaPostService])
          intent.putExtra("favorite_idea", convertObjectToJson(fav))

          startService(intent)
        }

        for (fav <- favoritesToDelete) {
          val intent = new Intent(getActivity, classOf[FavoriteIdeaDeleteService])
          intent.putExtra("favorite_idea", convertObjectToJson(fav))

          startService(intent)
        }
      })



      if (App.DEBUG) Log.d("MainActivity", "---------- REFRESH RESOURCES FINISHED ------------")
    }


  }

  private def getFavoritesToUpload(): List[FavoriteIdea] = {
    // Create select clause
    val select: String = makeWhereClause((FavoriteIdeaColumns.KEY_IS_NEW, true), (FavoriteIdeaColumns.KEY_IS_SYNCING, false), (FavoriteIdeaColumns.KEY_IS_DELETED, false))

    getFavoriteIdeas(select)
  }

  private def getFavoritesToDelete(): List[FavoriteIdea] = {
    // Create select clause
    val select = makeWhereClause((FavoriteIdeaColumns.KEY_IS_DELETED, true))

    getFavoriteIdeas(select)
  }

  private def getFavoriteIdeas(select: String): List[FavoriteIdea] = {
    var ideas = List[FavoriteIdea]()


    getResolver() match {
      case None => if (App.DEBUG) Log.d("FavoriteIdeaFragment", "No activity was found!")
      case Some(resolver) => {
        // Get cursor
        val cursor = getObjects(resolver,
          null, select, null, null)

        val keyIdIndex = cursor.getColumnIndexOrThrow(FavoriteIdeaColumns.KEY_ID)
        val keyIdeaIndex = cursor.getColumnIndexOrThrow(FavoriteIdeaColumns.KEY_IDEA)
        val keyUriIndex = cursor.getColumnIndexOrThrow(FavoriteIdeaColumns.KEY_RESOURCE_URI)

        while (cursor.moveToNext()) {
          ideas = ideas :+ new FavoriteIdea(cursor.getString(keyIdIndex), cursor.getString(keyIdeaIndex), cursor.getString(keyUriIndex))
        }

        cursor.close()
      }
    }

    ideas
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
