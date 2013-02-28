package com.limeblast.mydeatree

import android.support.v4.app.{ListFragment, FragmentActivity, LoaderManager}
import android.support.v4.widget.SimpleCursorAdapter
import android.database.Cursor
import android.os.Bundle
import android.support.v4.content.{CursorLoader, Loader}
import android.content.Intent
import android.app.SearchManager

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 1/18/13
 * Time: 10:11 AM
 * To change this template use File | Settings | File Templates.
 */
object SearchResultsActivity {
  val QUERY_EXTRA_KEY = "QUERY_EXTRA_KEY"
}

class SearchResultsActivity extends FragmentActivity {
  var searchFragment: SearchListFragment = _


  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.search_ideas)

    val fragmentManager = getSupportFragmentManager
    searchFragment = fragmentManager.findFragmentById(R.id.search_list_fragment).asInstanceOf[SearchListFragment]

    parseIntent(getIntent)
  }


  override def onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    parseIntent(intent)
  }

  private def parseIntent(intent: Intent) {
    if (Intent.ACTION_SEARCH.equals(intent.getAction)) {
      val searchQuery = intent.getStringExtra(SearchManager.QUERY)
      // Perform search
      performSearch(searchQuery)
    }
  }

  private def performSearch(query: String) {
    val args = new Bundle()
    args.putString(SearchResultsActivity.QUERY_EXTRA_KEY, query)

    // Restart the cursor loader to execute the new query
    searchFragment.restartLoader(args)
  }

}

class SearchListFragment extends ListFragment with LoaderManager.LoaderCallbacks[Cursor] {
  private var adapter: SimpleCursorAdapter = _

  override def onActivityCreated(savedInstanceState: Bundle) {
    super.onActivityCreated(savedInstanceState)

    adapter = new SimpleCursorAdapter(getActivity, android.R.layout.simple_list_item_1, null,
      Array[String](IdeaHelper.KEY_TITLE),
      Array[Int](android.R.id.text1))

    setListAdapter(adapter)
    getLoaderManager.initLoader(0, null, this)
  }

  def restartLoader(args: Bundle) {
    getLoaderManager.restartLoader(0, args, this)
  }

  /**
   * Loader methods
   */
  def onCreateLoader(id: Int, bundle: Bundle): Loader[Cursor] = {
    var query = "0"

    // Extract the search query from the arguments
    if (bundle != null)
      query = bundle.getString(SearchResultsActivity.QUERY_EXTRA_KEY)

    // Construct the new query in the form of a Cursor Loader
    val projection = Array[String](IdeaHelper.KEY_ID, IdeaHelper.KEY_TITLE)

    val where = IdeaHelper.KEY_TITLE + " LIKE \"%" + query + "%\""
    val whereArgs = null
    val sortOrder = IdeaHelper.KEY_TITLE + " COLLATE LOCALIZED ASC"

    // Create the new Cursor Loader
    new CursorLoader(getActivity.getApplicationContext, RESTfulProvider.CONTENT_URI, projection, where, whereArgs, sortOrder)
  }

  def onLoadFinished(loader: Loader[Cursor], cursor: Cursor) {
     adapter.swapCursor(cursor)
  }

  def onLoaderReset(loader: Loader[Cursor]) {
    // Remove the existing result Cursor from the List Adapter
    adapter.swapCursor(null)
  }
}
