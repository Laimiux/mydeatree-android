package com.limeblast.mydeatree.fragments

import com.actionbarsherlock.app.SherlockFragment
import android.support.v4.app.LoaderManager
import android.database.Cursor
import android.support.v4.content.{CursorLoader, Loader}
import android.os.{Handler, Bundle}
import java.util
import android.view.{KeyEvent, View, ViewGroup, LayoutInflater}
import android.widget.{AdapterView, Toast, ListView, ArrayAdapter}


import android.util.Log
import util.Collections
import com.actionbarsherlock.view.MenuItem
import android.content
import android.app.AlertDialog
import content.{Intent, DialogInterface}

import com.limeblast.mydeatree._
import adapters.PublicIdeaListAdapter
import com.limeblast.mydeatree.Helpers._
import com.limeblast.mydeatree.activities.NewIdeaActivity
import com.limeblast.mydeatree.AppSettings._
import providers.{PrivateIdeaProvider, PublicIdeaProvider}
import services.PublicIdeaSyncService
import com.limeblast.androidhelpers.ScalifiedTraitModule
import android.app.AlertDialog.Builder
import annotation.switch
import scala.Some
import storage.{PrivateIdeaTableInfo, PublicIdeaTableInfo, PublicIdeaDatabaseModule}
import com.limeblast.rest.JsonModule
import scala.Some
import scala.Some

class PublicIdeaFragment extends SherlockFragment
with HasParentState[PublicIdea]
with LoaderManager.LoaderCallbacks[Cursor] with ScalifiedTraitModule with JsonModule {
  private val APP_TAG = "PUBLIC_IDEA_FRAGMENT"

  private val publicIdeas: util.ArrayList[PublicIdea] = new util.ArrayList()
  private lazy val handler: Handler = new Handler()

  private var publicIdeaListView: ListView = _
  var aa: ArrayAdapter[PublicIdea] = _


  // Defines how to sort ideas
  private var sort_by = 0


  //-------------------------------------------------------\\
  //----------- PARENT STATE IMPLEMENTATION ---------------\\
  //-------------------------------------------------------\\
  var parentObject: Option[PublicIdea] = None

  override def setParent(parent: Option[PublicIdea]) {
    super.setParent(parent)

    refresh()

    shortToast("setParent method call has been handled.")(getActivity)
    if (App.DEBUG) Log.d(APP_TAG, "setParent method call has been handled.")
  }


  private def moveToPreviousParent() {
    parentObject match {
      case Some(idea) => moveToPreviousParent(idea)
      case _ =>
    }
  }


  private def moveToPreviousParent(currentParent: PublicIdea) {
    if (currentParent.parent != null) {
      setParent(getIdea(currentParent.parent))
      //AppSettings.PRIVATE_PARENT_IDEA = Some(getIdea(currentParent.parent))
      //setHeaderIdea(currentParent)
    } else {
      setParent(None)
      //setDefaultHeader()
    }
    refresh()
  }

  private def getIdea(resource_uri: String): Option[PublicIdea] = getResolver() match {
    case None => {
      if (App.DEBUG) Log.d(APP_TAG, "Could not retrieve ContentResolver in getIdea method")

      None
    }
    case Some(cr) => {
      val selection = PrivateIdeaTableInfo.KEY_RESOURCE_URI + "='" + resource_uri + "'"

      val cursor = cr.query(PrivateIdeaProvider.CONTENT_URI, null, selection, null, null)

      val keyTitleIndex = cursor.getColumnIndexOrThrow(PublicIdeaTableInfo.KEY_TITLE)
      val keyTextIndex = cursor.getColumnIndexOrThrow(PublicIdeaTableInfo.KEY_TEXT)
      val keyResourceUriIndex = cursor.getColumnIndexOrThrow(PublicIdeaTableInfo.KEY_RESOURCE_URI)
      val keyModifiedDateIndex = cursor.getColumnIndexOrThrow(PublicIdeaTableInfo.KEY_MODIFIED_DATE)
      val keyCreatedDateIndex = cursor.getColumnIndexOrThrow(PublicIdeaTableInfo.KEY_CREATED_DATE)
      val keyParentIndex = cursor.getColumnIndexOrThrow(PublicIdeaTableInfo.KEY_PARENT)
      val keyIdIndex = cursor.getColumnIndexOrThrow(PublicIdeaTableInfo.KEY_ID)
      val keyOwnerIndex = cursor.getColumnIndexOrThrow(PublicIdeaTableInfo.KEY_OWNER)
      val keyChildCount = cursor.getColumnIndexOrThrow(PublicIdeaTableInfo.KEY_CHILDREN_COUNT)


      if (App.DEBUG) Log.d(APP_TAG, cursor.getColumnNames.toString)
      //var idea: PublicIdea = null

      // Should loop only once
      val idea = if (cursor.moveToNext()) {
        Some(new PublicIdea(cursor.getString(keyTitleIndex),
          cursor.getString(keyTextIndex),
          cursor.getString(keyIdIndex),
          cursor.getString(keyParentIndex),
          cursor.getString(keyCreatedDateIndex),
          cursor.getString(keyModifiedDateIndex),
          cursor.getString(keyResourceUriIndex),
          new Owner(cursor.getString(keyOwnerIndex)),
          cursor.getInt(keyChildCount)))
      } else None

      cursor.close()

      idea
    }
  }

  //-------------------------------------------------------\\
  //------------ FRAGMENT LIFECYCLE EVENTS ----------------\\
  //-------------------------------------------------------\\
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val view = inflater.inflate(R.layout.public_ideas_layout, container, false)

    view.setFocusableInTouchMode(true)
    view.requestFocus()

    publicIdeaListView = view.findViewById(R.id.public_idea_list).asInstanceOf[ListView]


    view.setOnKeyListener((code: Int, event: KeyEvent) => onKey(code, event))

    view
  }


  override def onActivityCreated(savedInstanceState: Bundle) {
    super.onActivityCreated(savedInstanceState)

    if (savedInstanceState != null) {
      val parentObjectJson = savedInstanceState.getString("parent_object")


      if (App.DEBUG) Log.d(APP_TAG, "Parent object json is " + parentObjectJson)
      if (parentObjectJson != null && parentObjectJson != "")
        parentObject = Some(getMainObject(parentObjectJson, classOf[PublicIdea]))
    }

    if (App.DEBUG) Log.d(APP_TAG, "Parent object is " + parentObject)

    setHasOptionsMenu(true)

    sort_by = getSavedSortStatus()

    val layoutID = R.layout.public_idea_entry


    aa = new PublicIdeaListAdapter(this, layoutID, publicIdeas)

    publicIdeaListView.setAdapter(aa)

    // publicIdeaListView.setLongClickable(true)

    publicIdeaListView.onItemLongClick((aView, view, position, id) => handleIdeaListLongClick(aView, view, position, id))

    getLoaderManager.initLoader(0, null, this)

  }


  override def onSaveInstanceState(outState: Bundle) {
    if (App.DEBUG) Log.d(APP_TAG, "Saving PublicIdeaFragment instance")
    parentObject match {
      case None => // There is no parent to save
      case Some(parent) => outState.putString("parent_object", parent.toJson())
    }
    super.onSaveInstanceState(outState)
  }

  override def onResume() {
    // Should sync every time user comes back to the fragment.
    if (!isServiceRunning(classOf[PublicIdeaSyncService].getName)(getActivity))
      refreshPublicIdeas()

    refresh()
    super.onResume()
  }


  private def startNewIdeaActivity() {
    val intent = new content.Intent(getActivity, classOf[NewIdeaActivity])
    intent.putExtra("public", true)
    startActivity(intent)
    // startActivity(classOf[NewIdeaActivity].putExtra("public", true))

  }

  //-------------------------------------------------------\\
  //----------- FUNCTIONS FOR RERESHING IDEAS -------------\\
  //-------------------------------------------------------\\
  private def refreshPublicIdeas() {

    val intent = new Intent(getActivity, classOf[PublicIdeaSyncService])

    // Implicitly adding a ResultReceiver to the intent
    intent.putExtra(PUBLIC_IDEA_RESULT_RECEIVER, (resultCode: Int, resultData: Bundle) => {
      if (resultCode == 0) {
        refresh()
      }
    })

    // Start service
    startService(intent)
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
    sortIdeas
  }

  def sortIdeas = publicIdeas.synchronized {
    IdeaSortStrategy.getStrategy[PublicIdea](sort_by) match {
      case None =>
      case Some(strategy) => {
        strategy.sort(publicIdeas)
        handler.post(aa.notifyDataSetChanged())
      }
    }
  }


  def getSavedSortStatus(): Int = getActivity.getDefaultPreferences() match {
    case Some(preferences) => preferences.getInt(PREF_PUBLIC_SORT, 0)
    case None => {
      if (App.DEBUG) Log.d(APP_TAG, "getSavedSortStatus could not retrieve preferences.")
      0
    }
  }


  def updateSortStatus(): Boolean = getActivity.getDefaultPreferences() match {
    case Some(preferences) => preferences.edit().putInt(PREF_PUBLIC_SORT, sort_by).commit()
    case None => {
      if (App.DEBUG) Log.d(APP_TAG, "updateSortStatus failed to retrieve preferences.")
      false
    }
  }


  //-------------------------------------------------------\\
  //----------- BUILDERS FOR VARIOUS DIALOGS --------------\\
  //-------------------------------------------------------\\

  def openSortOptions() {
    val builder = new AlertDialog.Builder(getActivity)
    builder.setTitle(R.string.sort_by)
    builder.setSingleChoiceItems(R.array.sort_options,
      sort_by, (dialog: DialogInterface, which: Int) => {
        sortIdeas(which)
        dialog.dismiss()
      })


    builder.show()
  }


  private def openPublicIdeaOwnerOptions(pIdea: PublicIdea) {
    val builder = new Builder(getActivity)
    builder.setTitle(pIdea.title)

    builder.setItems(R.array.public_idea_owner_options, (dialog: DialogInterface, which: Int) => (which: Int@switch) match {
      case z => getActivity.shortToast("Ain't shit handled at " + z)
    })

    builder.show()
  }

  def openPublicIdeaOptions(pIdea: PublicIdea) {
    if (App.DEBUG) Log.d(APP_TAG, "Creating public idea option menu.")

    val builder = new Builder(getActivity)
    builder.setTitle(pIdea.title + " by " + pIdea.owner.username)

    builder.setItems(R.array.public_idea_options, (dialog: DialogInterface, which: Int) => (which: Int@switch) match {
      case 0 => getActivity.shortToast("Share clicked")
      case 1 => getActivity.shortToast("Favorite clicked")
      case z => getActivity.shortToast("unhandled " + z)
    })


    builder.show()
  }


  //-------------------------------------------------------\\
  //------ HANDLING ACTIONS FROM VARIOUS LISTENERS --------\\
  //-------------------------------------------------------\\

  private def handleIdeaListLongClick(adapterView: AdapterView[_], view: View, position: Int, id: Long): Boolean = {
    if (App.DEBUG) Log.d(APP_TAG, "handleIdeaListLongClick has been called.")
    adapterView.getItemAtPosition(position) match {
      case publicIdea: PublicIdea => {
        if (App.USERNAME != publicIdea.owner.username)
          openPublicIdeaOptions(publicIdea)
        else
          openPublicIdeaOwnerOptions(publicIdea)
        true
      }
      case _ => {
        if (App.DEBUG) Log.d(APP_TAG, "There was a problem getting a public idea at position " + position)
        false
      }
    }
  }


  override def onOptionsItemSelected(item: MenuItem): Boolean = item.getItemId match {
    /* Sync public ideas */
    case R.id.menu_item_sync_public_ideas => {
      syncPublicIdeas()
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

  private def syncPublicIdeas() {
    if (isServiceRunning(classOf[PublicIdeaSyncService].getName)(getActivity)) {
      Toast.makeText(getActivity, "Already syncing public ideas...", Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(getActivity, "Starting public idea sync...", Toast.LENGTH_SHORT).show()
      refreshPublicIdeas()
    }
  }


  //-------------------------------------------------------\\
  //------------ HANDLE KEY PRESS EVENTS ------------------\\
  //-------------------------------------------------------\\
  def onKey(keyCode: Int, keyEvent: KeyEvent): Boolean =
    keyCode match {
      case KeyEvent.KEYCODE_BACK => {
        handleBackKey(keyEvent.getAction)
      }
      case _ => false
    }

  private def handleBackKey(keyAction: Int): Boolean = {
    parentObject match {
      case Some(idea) => {
        if (keyAction == KeyEvent.ACTION_UP) {
          if (App.DEBUG) Log.d(APP_TAG, "Back On Key Event is released.")

          moveToPreviousParent(idea)
        }
        true
      }
      case _ => false
    }
  }


  //-------------------------------------------------------\\
  //--------- LOADER MANAGER CALLBACK METHODS -------------\\
  //-------------------------------------------------------\\
  def onCreateLoader(id: Int, args: Bundle): Loader[Cursor] = {
    val select = parentObject match {
      case None => PublicIdeaTableInfo.KEY_PARENT + " IS NULL"
      case Some(parentIdea: PublicIdea) =>
        if (parentIdea.resource_uri != null)
          PublicIdeaTableInfo.KEY_PARENT + "='" + parentIdea.resource_uri + "'"
        else
          PublicIdeaTableInfo.KEY_PARENT + " IS NULL"
    }

    new CursorLoader(getActivity, PublicIdeaProvider.CONTENT_URI, null, select, null, null)
  }


  def onLoadFinished(loader: Loader[Cursor], cursor: Cursor) {
    val keyTitleIndex = cursor.getColumnIndexOrThrow(PublicIdeaTableInfo.KEY_TITLE)
    val keyTextIndex = cursor.getColumnIndexOrThrow(PublicIdeaTableInfo.KEY_TEXT)
    val keyResourceUriIndex = cursor.getColumnIndexOrThrow(PublicIdeaTableInfo.KEY_RESOURCE_URI)
    val keyModifiedDateIndex = cursor.getColumnIndexOrThrow(PublicIdeaTableInfo.KEY_MODIFIED_DATE)
    val keyCreatedDateIndex = cursor.getColumnIndexOrThrow(PublicIdeaTableInfo.KEY_CREATED_DATE)
    val keyIdIndex = cursor.getColumnIndexOrThrow(PublicIdeaTableInfo.KEY_ID)
    val keyOwnerIndex = cursor.getColumnIndexOrThrow(PublicIdeaTableInfo.KEY_OWNER)
    val keyParentIndex = cursor.getColumnIndexOrThrow(PublicIdeaTableInfo.KEY_PARENT)
    val keyChildrenIdea = cursor.getColumnIndexOrThrow(PublicIdeaTableInfo.KEY_CHILDREN_COUNT)


    publicIdeas.clear()


    while (cursor.moveToNext()) {
      val idea = new PublicIdea(cursor.getString(keyTitleIndex),
        cursor.getString(keyTextIndex),
        cursor.getString(keyIdIndex),
        cursor.getString(keyParentIndex),
        cursor.getString(keyCreatedDateIndex),
        cursor.getString(keyModifiedDateIndex),
        cursor.getString(keyResourceUriIndex),
        new Owner(cursor.getString(keyOwnerIndex)),
        cursor.getInt(keyChildrenIdea))

      publicIdeas.add(0, idea)
    }

    if (App.DEBUG) Log.d(APP_TAG, "Retrieved " + publicIdeas.size() + " public ideas")

    sortIdeas
  }

  def onLoaderReset(loader: Loader[Cursor]) {}

  //-------------------------------------------------------\\
  //------ END OF LOADER MANAGER CALLBACK METHODS ---------\\
  //-------------------------------------------------------\\
}
