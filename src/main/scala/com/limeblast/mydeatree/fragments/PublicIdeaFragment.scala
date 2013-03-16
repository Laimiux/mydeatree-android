package com.limeblast.mydeatree.fragments

import com.actionbarsherlock.app.SherlockFragment
import android.support.v4.app.LoaderManager
import android.database.Cursor
import android.support.v4.content.{CursorLoader, Loader}
import android.os.{Handler, Bundle}
import java.util
import android.view.{View, ViewGroup, LayoutInflater}
import android.widget.{Toast, ListView, ArrayAdapter}


import android.util.Log
import util.Collections
import com.actionbarsherlock.view.MenuItem
import android.content
import android.app.AlertDialog
import content.{Intent, DialogInterface}
import android.preference.PreferenceManager

import com.limeblast.androidhelpers.AndroidImplicits
import AndroidImplicits.{toRunnable, functionToResultReceicer}
import com.limeblast.mydeatree._
import com.limeblast.mydeatree.Helpers._
import com.limeblast.mydeatree.activities.NewIdeaActivity
import com.limeblast.mydeatree.AppSettings._
import providers.{PublicIdeaProvider, RESTfulProvider}
import services.PublicIdeaSyncService

class PublicIdeaFragment extends SherlockFragment with LoaderManager.LoaderCallbacks[Cursor] with PublicIdeaDatabaseModule {
  private val APP_TAG = "PUBLIC_IDEA_FRAGMENT"

  private val publicIdeas: util.ArrayList[PublicIdea] = new util.ArrayList()
  private lazy val handler: Handler = new Handler()

  private var publicIdeaListView: ListView = _
  var aa: ArrayAdapter[PublicIdea] = _

  // Defines how to sort ideas
  private var sort_by = 0

  // Currently not used
  var parent_idea: String = null

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val view = inflater.inflate(R.layout.public_ideas_layout, container, false)

    publicIdeaListView = view.findViewById(R.id.public_idea_list).asInstanceOf[ListView]

    view
  }


  override def onActivityCreated(savedInstanceState: Bundle) {
    super.onActivityCreated(savedInstanceState)

    setHasOptionsMenu(true)

    setSortStatus()

    val layoutID = R.layout.public_idea_entry
    aa = new PublicIdeaListAdapter(getActivity(), layoutID, publicIdeas)

    publicIdeaListView.setAdapter(aa)

    getLoaderManager.initLoader(0, null, this)

    if (isServiceRunning(classOf[PublicIdeaFragment].getName, getActivity)) {

    } else {
      refreshPublicIdeas()
    }
  }


  private def startNewIdeaActivity() {
    val intent = new content.Intent(getActivity, classOf[NewIdeaActivity])
    intent.putExtra("public", true)
    startActivity(intent)
  }

  //-------------------------------------------------------\\
  //----------- FUNCTIONS FOR RERESHING IDEAS -------------\\
  //-------------------------------------------------------\\
  private def refreshPublicIdeas() {

    val intent = new Intent(getActivity.getApplicationContext, classOf[PublicIdeaSyncService])

    // Implicitly adding a ResultReceiver to the intent
    intent.putExtra(PUBLIC_IDEA_RESULT_RECEIVER, (resultCode: Int, resultData: Bundle) => {
      if (resultCode == 0) {
        refresh()
      }
    })

    // Start service
    getActivity.startService(intent)
  }

  def refresh() {
    if (getActivity != null) {
      getLoaderManager.restartLoader(0, null, PublicIdeaFragment.this)
    }
  }


  //-------------------------------------------------------\\
  //------------ FUNCTIONS FOR SORTING IDEAS --------------\\
  //-------------------------------------------------------\\
  def sortIdeas(mode: Int) {
    sort_by = mode
    updateSortStatus()
    sortIdeas()
  }

  def sortIdeas() {
    publicIdeas.synchronized {
      if (sort_by == 0) {
        Collections.sort(publicIdeas, new IdeaComparatorByModifiedDate[PublicIdea]())
      } else if (sort_by == 1) {
        Collections.sort(publicIdeas, new IdeaComparatorByCreatedDate[PublicIdea]())
      } else if (sort_by == 2) {
        Collections.sort(publicIdeas, new IdeaComparatorByTitle[PublicIdea]())
      }
    }

    handler.post(aa.notifyDataSetChanged())
  }

  def setSortStatus() {
    val context = getActivity.getApplicationContext
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    sort_by = prefs.getInt(PREF_PUBLIC_SORT, 0)
  }

  def updateSortStatus() {
    val context = getActivity.getApplicationContext
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    prefs.edit().putInt(PREF_PUBLIC_SORT, sort_by).commit()
  }


  //-------------------------------------------------------\\
  //----------- BUILDERS FOR VARIOUS DIALOGS --------------\\
  //-------------------------------------------------------\\

  def openSortOptions() {

    val builder = new AlertDialog.Builder(getActivity)
    builder.setTitle(R.string.sort_by)
    builder.setSingleChoiceItems(R.array.sort_options,
      sort_by, new DialogInterface.OnClickListener() {
        def onClick(dialog: DialogInterface, which: Int) {
          sortIdeas(which)
          dialog.dismiss()
        }
      })

    builder.create().show()
  }


  //-------------------------------------------------------\\
  //--------- HANDLING ACTIONS FROM ACTIONBAR -------------\\
  //-------------------------------------------------------\\

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      /* Sync public ideas */
      case R.id.menu_item_sync_public_ideas => {
        if (isServiceRunning(classOf[PublicIdeaSyncService].getName, getActivity)) {
          Toast.makeText(getActivity, "Already syncing public ideas...", Toast.LENGTH_SHORT).show()
        } else {
          Toast.makeText(getActivity, "Starting public idea sync...", Toast.LENGTH_SHORT).show()
          refreshPublicIdeas()
        }
        true
      }
      /* new public idea */
      case R.id.menu_item_new_public_idea => {
        startNewIdeaActivity()
        true
      }
      /* sort public ideas */
      case R.id.menu_item_sort_public_ideas => {
        openSortOptions()
        true
      }
      case _ => super.onOptionsItemSelected(item)
    }
  }

  //-------------------------------------------------------\\
  //------------ FRAGMENT LIFECYCLE EVENTS ----------------\\
  //-------------------------------------------------------\\


  override def onResume() {
    refresh()
    super.onResume()
  }

  //-------------------------------------------------------\\
  //--------- LOADER MANAGER CALLBACK METHODS -------------\\
  //-------------------------------------------------------\\
  def onCreateLoader(id: Int, args: Bundle): Loader[Cursor] = {
    val select =
      if (parent_idea != null)
        PublicIdeaHelper.KEY_PARENT + "='" + parent_idea + "'"
      else
        PublicIdeaHelper.KEY_PARENT + " IS NULL"


    new CursorLoader(getActivity.getApplicationContext, PublicIdeaProvider.CONTENT_URI, null, select, null, null)
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


    publicIdeas.clear()


    while (cursor.moveToNext()) {
      val idea = new PublicIdea(cursor.getString(keyTitleIndex),
        cursor.getString(keyTextIndex),
        cursor.getString(keyIdIndex),
        cursor.getString(keyParentIndex),
        cursor.getString(keyCreatedDateIndex),
        cursor.getString(keyModifiedDateIndex),
        cursor.getString(keyResourceUriIndex),
        new Owner(cursor.getString(keyOwnerIndex)))

      publicIdeas.add(0, idea)
    }

    if (AppSettings.DEBUG) Log.d(APP_TAG, "Retrieved " + publicIdeas.size() + " public ideas")

    sortIdeas()
  }

  def onLoaderReset(loader: Loader[Cursor]) {}

  //-------------------------------------------------------\\
  //------ END OF LOADER MANAGER CALLBACK METHODS ---------\\
  //-------------------------------------------------------\\
}
