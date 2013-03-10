package com.limeblast.mydeatree.fragments

import android.support.v4.app.LoaderManager
import android.os.Bundle
import android.widget._
import java.util
import concurrent.ops._
import scala.collection.JavaConversions._

import android.view.{KeyEvent, ViewGroup, LayoutInflater, View}
import android.util.Log

import util.Collections
import android.database.Cursor
import android.support.v4.content.{CursorLoader, Loader}

import android.net.Uri

import android.app.AlertDialog

import android.content
import android.view.View.OnKeyListener

import content.{ContentValues, DialogInterface, Intent}
import android.preference.PreferenceManager

import com.limeblast.androidhelpers.{ScalaHandler, AndroidImplicits, AndroidHelpers}
import AndroidImplicits.{functionToResultReceicer, functionToLongListener, functionToDialogOnClickListener}


import com.actionbarsherlock.app.{SherlockFragmentActivity, SherlockListFragment}
import com.actionbarsherlock.view.MenuItem
import com.limeblast.mydeatree._
import com.limeblast.mydeatree.activities.{IdeaEditActivity, NewIdeaActivity}
import com.limeblast.mydeatree.AppSettings._
import com.limeblast.mydeatree.providers.RESTfulProvider
import scala.Some
import scala.Some
import scala.Some
import scala.Some
import scala.Some
import scala.Some
import scala.Some
import scala.Some
import com.limeblast.mydeatree.Helpers._
import scala.Some
import scala.Some
import scala.Some
import scala.Some

class PrivateIdeaListFragment extends SherlockListFragment with LoaderManager.LoaderCallbacks[Cursor]
with OnKeyListener {

  var aa: ArrayAdapter[Idea] = _

  private var handler: ScalaHandler = _

  // Sorting status
  var sort_by = 0

  // Array list to keep all the private ideas
  lazy val privateIdeas: util.ArrayList[Idea] = new util.ArrayList()
  // Array to keep all the headers that are currently used
  private val headers: util.ArrayList[View] = new util.ArrayList[View]()

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // Checks if user is logged in, if not throws an exception.
    if (getUsername(getActivity.getApplicationContext).equals("")) {
      throw new IllegalStateException("This fragment shouldn't be created when there is no username.")
    }

  }


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    if (AppSettings.DEBUG) Log.d(APP_TAG, "Creating PrivateIdeaListFragment view")
    val fragmentView = inflater.inflate(R.layout.private_idea_list_layout, container, false)

    fragmentView.setFocusableInTouchMode(true)
    fragmentView.requestFocus()

    // This listener controls the idea exploration,
    // where back key goes back to the previous parent idea
    fragmentView.setOnKeyListener(this)

    fragmentView
  }

  override def onActivityCreated(savedInstanceState: Bundle) {
    super.onActivityCreated(savedInstanceState)

    setHasOptionsMenu(true)

    setSortStatus()

    handler = new ScalaHandler()


    if (aa != null) {
      aa.clear()
      setListAdapter(null)
    }

    AppSettings.PRIVATE_PARENT_IDEA match {
      case Some(idea) => setHeaderIdea(idea)
      case None => setDefaultHeader()
    }

    val layoutID = R.layout.private_idea_entry
    aa = new IdeaListAdapter(getActivity(), layoutID, privateIdeas)
    setListAdapter(aa)

    getListView.setOnItemLongClickListener((parent: AdapterView[_], view: View, position: Int, id: Long) => {
      getListView.getItemAtPosition(position) match {
        case idea: Idea => showPrivateIdeaOptions(idea)
        case _ => if (AppSettings.DEBUG) Log.d(APP_TAG, "Some unknown object was selected")
      }
      true
    })

    getLoaderManager.initLoader(0, null, this)

    // If ideas are syncing show loading icon.
    // if (isServiceRunning(classOf[PrivateIdeaSyncService].getName, getActivity))
    //showIndeterminedProgress
  }


  private def clearOldHeaders() {
    for (view <- headers) {
      getListView.removeHeaderView(view)
    }
    headers.clear()
  }

  private def setHeaderIdea(idea: Idea) {
    val headerView = getActivity.getLayoutInflater.inflate(R.layout.private_idea_header, null)

    headerView.findViewById(R.id.private_header_idea_title) match {
      case tv: TextView => tv.setText(idea.title)
    }

    headerView.findViewById(R.id.private_header_idea_text) match {
      case tv: TextView => tv.setText(idea.text)
    }

    clearOldHeaders()

    setListAdapter(null)

    getListView.addHeaderView(headerView)

    headers.add(headerView)

    setListAdapter(aa)

  }


  /*
   * Creates a default list header
   */
  private def setDefaultHeader() {
    clearOldHeaders()

    val header = getActivity.getLayoutInflater().inflate(R.layout.private_idea_empty_header, null)

    setListAdapter(null)

    getListView.addHeaderView(header)
    headers.add(header)

    setListAdapter(aa)
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) =
    l.getItemAtPosition(position) match {
      case i: Idea => {
        AppSettings.PRIVATE_PARENT_IDEA = Some(i)
        setHeaderIdea(i)
        refresh()
      }
      case _ => super.onListItemClick(l, v, position, id)
    }


  /* Move this function eventually */

  private def getIdea(resource_uri: String): Idea = {
    val cr = getActivity.getContentResolver

    val selection = IdeaHelper.KEY_RESOURCE_URI + "='" + resource_uri + "'"

    val cursor = cr.query(RESTfulProvider.CONTENT_URI, null, selection, null, null)

    val keyTitleIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_TITLE)
    val keyTextIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_TEXT)
    val keyResourceUriIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_RESOURCE_URI)
    val keyModifiedDateIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_MODIFIED_DATE)
    val keyCreatedDateIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_CREATED_DATE)
    val keyParentIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_PARENT)
    val keyIdIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_ID)
    //val keyOwnerIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_OWNER)
    val keyPublicIndex = cursor.getColumnIndexOrThrow(IdeaHelper.KEY_PUBLIC)


    if (AppSettings.DEBUG) Log.d(APP_TAG, cursor.getColumnNames.toString)
    var idea: Idea = null

    // Should loop only once
    while (cursor.moveToNext()) {
      idea = new Idea(cursor.getString(keyTitleIndex),
        cursor.getString(keyTextIndex),
        cursor.getString(keyIdIndex),
        cursor.getString(keyParentIndex),
        cursor.getString(keyCreatedDateIndex),
        cursor.getString(keyModifiedDateIndex),
        cursor.getString(keyResourceUriIndex),
        cursor.getInt(keyPublicIndex) > 0)
    }

    cursor.close()
    idea
  }

  def removeIdeaRequest(idea: Idea) {
    // This means idea is on server
    if (idea.id != null) {
      // Mark idea for deletion
      val resolver = getActivity.getContentResolver
      val where = IdeaHelper.KEY_ID + "=" + idea.id

      val values = new ContentValues()
      values.put(IdeaHelper.KEY_IS_IDEA_DELETED, true)

      resolver.update(RESTfulProvider.CONTENT_URI, values, where, null)

      // If there is internet connection
      // start service to delete the idea
      if (AndroidHelpers.isOnline(getActivity)) {

        val intent = new Intent(getActivity, classOf[IdeaDeleteService])
        intent.putExtra("idea", JsonWrapper.convertObjectToJson(idea))
        getActivity.startService(intent)
      }
    } else {
      // This means we just need to get rid of this idea from the database
      val resolver = getActivity.getContentResolver
      val where = IdeaHelper.KEY_TITLE + "='" + idea.title + "' AND " + IdeaHelper.KEY_TEXT +
        "='" + idea.text + "' AND " + IdeaHelper.KEY_CREATED_DATE + "='" + idea.created_date + "'"

      resolver.delete(RESTfulProvider.CONTENT_URI, where, null)
    }


    handler.post(refresh)
  }


  //-------------------------------------------------------\\
  //------------ FUNCTIONS FOR SORTING IDEAS --------------\\
  //-------------------------------------------------------\\

  def sortPrivateIdeas(sort: Int) {
    sort_by = sort
    updateSortStatus()
    sortIdeas()
  }

  def sortIdeas() {
    privateIdeas.synchronized {
      if (sort_by == 0) {
        Collections.sort(privateIdeas, new IdeaComparatorByModifiedDate[Idea]())
      } else if (sort_by == 1) {
        Collections.sort(privateIdeas, new IdeaComparatorByCreatedDate[Idea]())
      } else if (sort_by == 2) {
        Collections.sort(privateIdeas, new IdeaComparatorByTitle[Idea]())
      }
    }

    handler.post(aa.notifyDataSetChanged())
  }

  def setSortStatus() {
    val context = getActivity.getApplicationContext
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    sort_by = prefs.getInt(PREF_PRIVATE_SORT, 0)
  }

  def updateSortStatus() {
    val context = getActivity.getApplicationContext
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    prefs.edit().putInt(PREF_PRIVATE_SORT, sort_by).commit()
  }


  def refresh() {
    if (getActivity != null) {
      getLoaderManager().restartLoader(0, null, this)
    }
    if (AppSettings.DEBUG) Log.d(APP_TAG, "PrivateIdeaListFragment refresh is finished.")
  }


  private def startNewIdeaActivity() {
    val intent = new content.Intent(getActivity, classOf[NewIdeaActivity])
    AppSettings.PRIVATE_PARENT_IDEA match {
      case Some(idea) => intent.putExtra("parent_uri", idea.resource_uri)
      case _ =>
    }

    startActivity(intent)
  }

  //-------------------------------------------------------\\
  //------------BUILDERS FOR VARIOUS DIALOG ---------------\\
  //-------------------------------------------------------\\
  private def showPrivateIdeaOptions(idea: Idea) {
    val builder = new AlertDialog.Builder(getActivity)
    builder.setTitle(idea.title)
    builder.setItems(R.array.private_idea_options, (dialog: DialogInterface, which: Int) => {
      if (which == 0) {
        // NEW CHILDREN IDEA
        AppSettings.PRIVATE_PARENT_IDEA = Some(idea)
        startNewIdeaActivity()
      }
      else if (which == 1) {
        //EDIT
        dialog.dismiss()
        val ideaJson = JsonWrapper.convertObjectToJson(idea)
        val intent = new Intent(getActivity, classOf[IdeaEditActivity])
        intent.putExtra("idea", ideaJson)
        startActivity(intent)
      } else if (which == 2) {
        // DELETE
        dialog.dismiss()
        showIdeaDeleteDialog(idea)
      }
    })


    builder.create().show()
  }

  private def showIdeaDeleteDialog(idea: Idea) {
    val builder = new AlertDialog.Builder(getActivity)
    builder.setTitle("Delete " + idea.title + "?")
    builder.setMessage("Are you sure you want to delete this idea?")
    builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
      def onClick(dialog: DialogInterface, arg: Int) {
        spawn {
          removeIdeaRequest(idea)
        }
        dialog.dismiss()
      }
    })

    builder.setNegativeButton(R.string.cancel, (dialog: DialogInterface, arg: Int) => dialog.cancel())

    builder.create().show()
  }


  private def openSortOptions() {
    val builder = new AlertDialog.Builder(getActivity)

    builder.setTitle(R.string.sort_by)
    builder.setSingleChoiceItems(R.array.sort_options,
      sort_by, (dialog: DialogInterface, which: Int) => {
        sortPrivateIdeas(which)
        dialog.dismiss()
      })

    builder.create().show()
  }

  //-------------------------------------------------------\\
  //------------ FUNCTIONS TO SHOW LOADING ----------------\\
  //-------------------------------------------------------\\
  private def showIndeterminedProgress() =
    getActivity match {
      case sherlock: SherlockFragmentActivity => {
        sherlock.setSupportProgressBarIndeterminateVisibility(true)
      }
      case _ =>
    }


  private def removeIndeterminedProgress() =
    getActivity match {
      case sherlock: SherlockFragmentActivity => {
        sherlock.setSupportProgressBarIndeterminateVisibility(false)
      }
      case _ =>
    }


  //-------------------------------------------------------\\
  //------------ FRAGMENT LIFECYCLE EVENTS ----------------\\
  //-------------------------------------------------------\\


  override def onResume() {
    if (!IS_PERSONAL_IDEAS_SYNCING) {
      //IS_PERSONAL_IDEAS_SYNCING = true
      spawn {
        syncIfNecessary()
        IS_PERSONAL_IDEAS_SYNCING = false
      }
    }
    refresh()
    super.onResume()
  }

  //-------------------------------------------------------\\
  //--------- HANDLING ACTIONS FROM ACTIONBAR -------------\\
  //-------------------------------------------------------\\
  override def onOptionsItemSelected(item: MenuItem): Boolean =
    item.getItemId match {
      /* Sync private ideas */
      case R.id.menu_item_sync_private_ideas => {
        startPrivateIdeaSync()
        true
      }
      /* New private idea event */
      case R.id.menu_item_new_private_idea => {
        startNewIdeaActivity()
        true
      }
      /* Sort private ideas */
      case R.id.menu_item_sort_private_ideas => {
        openSortOptions()
        true
      }
      case _ => super.onOptionsItemSelected(item)
    }


  //-------------------------------------------------------\\
  //--------- LOADER MANAGER CALLBACK METHODS -------------\\
  //-------------------------------------------------------\\
  def onCreateLoader(id: Int, args: Bundle): Loader[Cursor] = {
    val uri = Uri.withAppendedPath(RESTfulProvider.CONTENT_URI, "/" + USERNAME)

    var select = IdeaHelper.KEY_IS_IDEA_DELETED + "=0"

    AppSettings.PRIVATE_PARENT_IDEA match {
      case Some(idea) => select += " AND " + IdeaHelper.KEY_PARENT + "='" + idea.resource_uri + "'"
      case None => select += " AND " + IdeaHelper.KEY_PARENT + " IS NULL"
    }


    new CursorLoader(getActivity.getApplicationContext, uri, null, select, null, null)
  }

  def onLoadFinished(loader: Loader[Cursor], cursor: Cursor) {

    privateIdeas.clear()

    val ideas = IdeaTableHelper.getIdeas(cursor)
    privateIdeas.addAll(ideas)

    sortIdeas()
  }

  def onLoaderReset(loader: Loader[Cursor]) {}

  //-------------------------------------------------------\\
  //------ END OF LOADER MANAGER CALLBACK METHODS ---------\\
  //-------------------------------------------------------\\


  // Key Listener that listens for back key and moves back within the private idea hierarchy
  def onKey(view: View, keyCode: Int, keyEvent: KeyEvent): Boolean =
    keyCode match {
      case KeyEvent.KEYCODE_BACK => {
        AppSettings.PRIVATE_PARENT_IDEA match {
          case Some(idea) => {
            if (keyEvent.getAction == KeyEvent.ACTION_UP) {
              if (AppSettings.DEBUG) Log.d(APP_TAG, "Back On Key Event is released.")

              if (idea.parent != null) {
                AppSettings.PRIVATE_PARENT_IDEA = Some(getIdea(idea.parent))
                setHeaderIdea(idea)
              } else {
                AppSettings.PRIVATE_PARENT_IDEA = None
                setDefaultHeader()
              }
              refresh()
            }
            true
          }
          case _ => false
        }
      }
      case _ => false
    }


  //-------------------------------------------------------\\
  //-------------- Synchronize Functions ------------------\\
  //-------------------------------------------------------\\
  private def startPrivateIdeaSync() {
    if (isServiceRunning(classOf[PrivateIdeaSyncService].getName, getActivity)) {
      Toast.makeText(getActivity, "Already syncing personal ideas", Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(getActivity, "Starting syncing of personal ideas...", Toast.LENGTH_SHORT).show()
      // Create the intent for the service
      val intent = new Intent(getActivity, classOf[PrivateIdeaSyncService])
      // Add receiver to the intent
      intent.putExtra(PRIVATE_IDEA_RESULT_RECEIVER, (resultCode: Int, resultData: Bundle) => {
        handler.post(refresh())
      })
      // Start service
      getActivity.startService(intent)

      //showIndeterminedProgress()
    }
  }

  private def syncIfNecessary() {
    Log.d(APP_TAG, "syncIfNecessary called")
    // Do this only if there is network connection
    if (AndroidHelpers.isOnline(getActivity)) {
      // Get all ideas
      val ideasToUpload = getIdeasToUpload()
      val ideasToUpdate = getIdeasToUpdate()
      val ideasToDelete = getIdeasToDelete()


      Log.d(APP_TAG, "There are " + ideasToUpdate.size() + " ideas to update")
      Log.d(APP_TAG, "There are " + ideasToUpload.size() + " ideas to upload")
      Log.d(APP_TAG, "There are " + ideasToDelete.size() + " ideas to delete")

      for (idea <- ideasToUpload) {
        // New ideas have id that is null
        val ideaJson = JsonWrapper.convertObjectToJson(idea)
        val intent = new Intent(getActivity, classOf[IdeaCreateService])
        intent.putExtra("idea", ideaJson)

        getActivity.startService(intent)
      }

      for (idea <- ideasToUpdate) {
        val ideaJson = JsonWrapper.convertObjectToJson(idea)
        val intent = new Intent(getActivity, classOf[IdeaUpdateService])
        intent.putExtra("idea", ideaJson)

        getActivity.startService(intent)
      }

      for (idea <- ideasToDelete) {
        val ideaJson = JsonWrapper.convertObjectToJson(idea)
        val intent = new Intent(getActivity, classOf[IdeaDeleteService])
        intent.putExtra("idea", ideaJson)
        getActivity.startService(intent)
      }

      handler.post(refresh)
    }
  }

  private def getIdeasToUpdate(): util.ArrayList[Idea] = {
    val resolver = getActivity.getContentResolver

    val select = IdeaHelper.KEY_IS_IDEA_EDITED + "=1 AND " + IdeaHelper.KEY_IS_IDEA_SYNCING + "=0"
    val cursor = resolver.query(RESTfulProvider.CONTENT_URI, null, select, null, null)

    IdeaTableHelper.getIdeas(cursor)
  }

  private def getIdeasToDelete(): util.ArrayList[Idea] = {
    val resolver = getActivity.getContentResolver
    val select = IdeaHelper.KEY_IS_IDEA_DELETED + "=1 AND " + IdeaHelper.KEY_IS_IDEA_SYNCING + "=0"
    val cursor = resolver.query(RESTfulProvider.CONTENT_URI, null, select, null, null)

    IdeaTableHelper.getIdeas(cursor)
  }

  private def getIdeasToUpload(): util.ArrayList[Idea] = {
    val resolver = getActivity.getContentResolver
    val select = IdeaHelper.KEY_IS_IDEA_NEW + "=1 AND " + IdeaHelper.KEY_IS_IDEA_SYNCING + "=0"
    val cursor = resolver.query(RESTfulProvider.CONTENT_URI, null, select, null, null)

    IdeaTableHelper.getIdeas(cursor)
  }
}
