package com.limeblast.mydeatree.fragments

import com.actionbarsherlock.app.{SherlockListFragment, SherlockFragment}
import android.view.{View, ViewGroup, LayoutInflater}
import android.os.Bundle
import com.limeblast.mydeatree.{R, AppSettings}
import android.util.Log
import com.limeblast.mydeatree.AppSettings._
import android.support.v4.app.LoaderManager
import android.database.Cursor
import android.support.v4.content.Loader

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 3/15/13
 * Time: 12:32 PM
 * To change this template use File | Settings | File Templates.
 */
class FavoriteIdeaFragment extends SherlockListFragment with LoaderManager.LoaderCallbacks[Cursor] {
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    //if (AppSettings.DEBUG) Log.d(APP_TAG, "Creating PrivateIdeaListFragment view")
    val fragmentView = inflater.inflate(R.layout.favorite_idea_layout, container, false)

    fragmentView.setFocusableInTouchMode(true)
    fragmentView.requestFocus()


    fragmentView

  }

  //-------------------------------------------------------\\
  //--------- LOADER MANAGER CALLBACK METHODS -------------\\
  //-------------------------------------------------------\\
  def onCreateLoader(p1: Int, p2: Bundle): Loader[Cursor] = ???

  def onLoadFinished(p1: Loader[Cursor], p2: Cursor) {}

  def onLoaderReset(p1: Loader[Cursor]) {}
  //-------------------------------------------------------\\
  //------ END OF LOADER MANAGER CALLBACK METHODS ---------\\
  //-------------------------------------------------------\\

}
