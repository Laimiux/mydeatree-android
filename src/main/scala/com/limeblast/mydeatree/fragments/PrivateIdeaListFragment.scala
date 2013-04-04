package com.limeblast.mydeatree.fragments

import android.support.v4.app.LoaderManager
import android.os.{Handler, Bundle}
import android.widget._
import java.util
import concurrent.ops._
import scala.collection.JavaConversions._

import android.view.{KeyEvent, ViewGroup, LayoutInflater, View}
import android.util.Log

import util.Collections
import android.database.Cursor
import android.support.v4.content.{CursorLoader, Loader}


import android.app.{Activity, AlertDialog}

import android.content

import content._

import com.limeblast.androidhelpers.{AlertDialogHelper, WhereClauseModule, ScalifiedTraitModule}


import com.actionbarsherlock.app.{SherlockFragmentActivity, SherlockListFragment}
import com.actionbarsherlock.view.MenuItem
import com.limeblast.mydeatree._
import adapters.IdeaListAdapter
import com.limeblast.mydeatree.activities.{IdeaEditActivity, NewIdeaActivity}
import com.limeblast.mydeatree.AppSettings._
import com.limeblast.mydeatree.providers.PrivateIdeaProvider

import services.{PrivateIdeaSyncService, IdeaUpdateService, IdeaDeleteService, IdeaCreateService}
import com.limeblast.rest.JsonModule
import android.app.AlertDialog.Builder
import scala.Some
import annotation.switch
import storage.PrivateIdeaTableInfo

class PrivateIdeaListFragment extends SherlockListFragment
with LoaderManager.LoaderCallbacks[Cursor] with JsonModule with PersonalIdeaGetModule
with ScalifiedTraitModule with WhereClauseModule {

  var aa: ArrayAdapter[Idea] = _

  private var handler: Handler = _

  // Sorting status
  // var sort_by = 0

  // Array list to keep all the private ideas
  lazy val privateIdeas: util.ArrayList[Idea] = new util.ArrayList()
  // Array to keep all the headers that are currently used
  private val headers: util.ArrayList[View] = new util.ArrayList[View]()


  //-------------------------------------------------------\\
  //----------- FRAGMENT LIFECYCLE FUNCTIONS --------------\\
  //-------------------------------------------------------\\

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // Checks if user is logged in, if not throws an exception.
    if (App.getUsername(getActivity.getApplicationContext).equals(""))
      throw new IllegalStateException("This fragment shouldn't be created when there is no username.")


  }


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    if (App.DEBUG) Log.d(APP_TAG, "Creating PrivateIdeaListFragment view")

    val fragmentView: View = inflater.inflate(R.layout.private_idea_list_layout, container, false)

    fragmentView.setFocusableInTouchMode(true)
    fragmentView.requestFocus()

    // This listener controls the idea exploration,
    // where back key goes back to the previous parent idea
    fragmentView.setOnKeyListener((keyCode: Int, event: KeyEvent) => onKey(keyCode, event))

    fragmentView
  }

  override def onActivityCreated(savedInstanceState: Bundle) {
    super.onActivityCreated(savedInstanceState)

    setHasOptionsMenu(true)

    handler = new Handler()


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

    getListView.onItemLongClick((parent: AdapterView[_], view: View, position: Int, id: Long) => {
      getListView.getItemAtPosition(position) match {
        case idea: Idea => showPrivateIdeaOptions(idea)
        case _ => if (App.DEBUG) Log.d(APP_TAG, "Some unknown object was selected")
      }
      true
    })

    getLoaderManager.initLoader(0, null, this)

    // If ideas are syncing show loading icon.
    // if (isServiceRunning(classOf[PrivateIdeaSyncService].getName, getActivity))
    //showIndeterminedProgress
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


  //-------------------------------------------------------\\
  //--------------- LIST HEADER FUNCTIONS -----------------\\
  //-------------------------------------------------------\\

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) =
    requestCode match {
      case NewIdeaActivity.NEW_IDEA_RESULT => {
        // if(resultCode == Activity.RESULT_CANCELED)
        //moveToPreviousParent()
      }
      case _ => super.onActivityResult(requestCode, resultCode, data)
    }

  /*{

  }*/

  private def clearOldHeaders() {
    for (view <- headers) {
      getListView.removeHeaderView(view)
    }
    headers.clear()
  }

  /**
   * Sets the list header to parent idea. NOTE: this function needs to be simplified.
   * @param idea Parent idea
   */
  private def setHeaderIdea(idea: Idea) {
    val headerView = getActivity.getLayoutInflater.inflate(R.layout.private_idea_header, null)


    // Set layout values to that of idea
    headerView.findViewById(R.id.private_header_idea_title) match {
      case tv: TextView => tv.setText(idea.title)
    }

    headerView.findViewById(R.id.private_header_idea_text) match {
      case tv: TextView => tv.setText(idea.text)
    }


    if (!idea.public)
      headerView.findViewById(R.id.private_header_idea_public).setVisibility(View.GONE)


    // Set the modified date
    val dateText = headerView.findViewById(R.id.private_header_idea_date).asInstanceOf[TextView]
    val date = Helpers.stringToDate(idea.modified_date)
    dateText.setText(Helpers.formatDate(date))

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


  private def moveToPreviousParent() {
    AppSettings.PRIVATE_PARENT_IDEA match {
      case Some(idea) => moveToPreviousParent(idea)
      case _ =>
    }
  }


  private def moveToPreviousParent(currentParent: Idea) {
    if (currentParent.parent != null) {
      AppSettings.PRIVATE_PARENT_IDEA = getIdea(currentParent.parent)
      setHeaderIdea(currentParent)
    } else {
      AppSettings.PRIVATE_PARENT_IDEA = None
      setDefaultHeader()
    }
    refresh()
  }


  /* Move this function eventually */

  private def getIdea(resource_uri: String): Option[Idea] = getResolver() match {
      case None => {
        if (App.DEBUG) Log.d(APP_TAG, "Could not retrieve ContentResolver in getIdea method")

        None
      }
      case Some(cr) => {
        val selection = PrivateIdeaTableInfo.KEY_RESOURCE_URI + "='" + resource_uri + "'"

        val cursor = cr.query(PrivateIdeaProvider.CONTENT_URI, null, selection, null, null)

        val keyTitleIndex = cursor.getColumnIndexOrThrow(PrivateIdeaTableInfo.KEY_TITLE)
        val keyTextIndex = cursor.getColumnIndexOrThrow(PrivateIdeaTableInfo.KEY_TEXT)
        val keyResourceUriIndex = cursor.getColumnIndexOrThrow(PrivateIdeaTableInfo.KEY_RESOURCE_URI)
        val keyModifiedDateIndex = cursor.getColumnIndexOrThrow(PrivateIdeaTableInfo.KEY_MODIFIED_DATE)
        val keyCreatedDateIndex = cursor.getColumnIndexOrThrow(PrivateIdeaTableInfo.KEY_CREATED_DATE)
        val keyParentIndex = cursor.getColumnIndexOrThrow(PrivateIdeaTableInfo.KEY_PARENT)
        val keyIdIndex = cursor.getColumnIndexOrThrow(PrivateIdeaTableInfo.KEY_ID)
        val keyPublicIndex = cursor.getColumnIndexOrThrow(PrivateIdeaTableInfo.KEY_PUBLIC)


        if (App.DEBUG) Log.d(APP_TAG, cursor.getColumnNames.toString)
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
        Some(idea)
      }
    }




  private def removeIdea(resolver: ContentResolver, idea: Idea) {
    // Check if idea is on server
    idea.id match {
      // Idea isn't on server
      case null => {
        // This means we just need to get rid of this idea from the database
        val where = makeWhereClause(PrivateIdeaTableInfo.KEY_TITLE -> idea.title, PrivateIdeaTableInfo.KEY_TEXT -> idea.text,
          PrivateIdeaTableInfo.KEY_CREATED_DATE -> idea.created_date)
        //val where = PrivateIdeaTableInfo.KEY_TITLE + "='" + idea.title + "' AND " + PrivateIdeaTableInfo.KEY_TEXT +
        //"='" + idea.text + "' AND " + PrivateIdeaTableInfo.KEY_CREATED_DATE + "='" + idea.created_date + "'"

        resolver.delete(PrivateIdeaProvider.CONTENT_URI, where, null)
      }
      case _ => {

        // Mark idea for deletion
        val where = makeWhereClause(PrivateIdeaTableInfo.KEY_ID -> idea.id)

        val values = new ContentValues()
        values.put(PrivateIdeaTableInfo.KEY_IS_IDEA_DELETED, true)

        resolver.update(PrivateIdeaProvider.CONTENT_URI, values, where, null)

        // If there is internet connection
        // start service to delete the idea
        if (isOnline(getActivity)) {

          val intent = new Intent(getActivity, classOf[IdeaDeleteService])
          intent.putExtra("idea", idea.toJson())
          getActivity.startService(intent)
        }

      }
    }
  }

  def removeIdeaRequest(idea: Idea) {
    getResolver() match {
      case None => if (App.DEBUG) Log.d(APP_TAG, "removeIdeaRequest couldn't get a resolver")
      case Some(resolver) => {
        removeIdea(resolver, idea)
        handler.post(refresh)
      }
    }
  }


  //-------------------------------------------------------\\
  //------------ FUNCTIONS FOR SORTING IDEAS --------------\\
  //-------------------------------------------------------\\

  def sortPrivateIdeas(sort: Int) {
    updateSortStatus(sort)
    sortIdeas(sort)
  }

  def sortIdeas(sort_by: Int) = privateIdeas.synchronized {
    IdeaSortStrategy.getStrategy[Idea](sort_by) match {
      case None =>
      case Some(strategy) => {
        strategy.sort(privateIdeas)
        handler.post(aa.notifyDataSetChanged())
      }
    }

  }


  def getSavedSortStatus() =
    getActivity.getDefaultPreferences() match {
      case Some(preferences) => preferences.getInt(PREF_PRIVATE_SORT, 0)
      case None => {
        if (App.DEBUG) Log.d(APP_TAG, "getSavedSortStatus couldn't get preferences ")
        0
      }
    }


  def updateSortStatus(sortValue: Int) =
    getActivity().getDefaultPreferences() match {
      case Some(preferences) => preferences.edit().putInt(PREF_PRIVATE_SORT, sortValue).commit()
      case None => if (App.DEBUG) Log.d(APP_TAG, "updateSortStatus function couldn't find activity")
    }


  def refresh() {
    if (getActivity != null) {
      getLoaderManager().restartLoader(0, null, this)
    }

    PRIVATE_PARENT_IDEA match {
      case None => setDefaultHeader()
      case Some(idea) => setHeaderIdea(idea)
    }

    if (App.DEBUG) Log.d(APP_TAG, "PrivateIdeaListFragment refresh is finished.")
  }


  private def startNewIdeaActivity() {
    val intent = new Intent(getActivity, classOf[NewIdeaActivity])
    AppSettings.PRIVATE_PARENT_IDEA match {
      case Some(idea) => intent.putExtra("parent_uri", idea.resource_uri)
      case _ =>
    }

    startActivityForResult(intent, NewIdeaActivity.NEW_IDEA_RESULT)
  }


  private def startEditActivity(idea: Idea) {
    val intent = new Intent(getActivity, classOf[IdeaEditActivity])
    intent.putExtra("idea", idea.toJson())
    startActivity(intent)
  }

  //-------------------------------------------------------\\
  //------------BUILDERS FOR VARIOUS DIALOG ---------------\\
  //-------------------------------------------------------\\
  private def showPrivateIdeaOptions(idea: Idea) {
    val builder: Builder = new AlertDialog.Builder(getActivity)
    builder.setTitle(idea.title)
    builder.setItems(R.array.private_idea_options, (dialog: DialogInterface, which: Int) => (which: Int@switch) match {
      case 0 => {
        // New children idea
        AppSettings.PRIVATE_PARENT_IDEA = Some(idea)
        startNewIdeaActivity()
      }
      case 1 => {
        //EDIT
        dialog.dismiss()

        startEditActivity(idea)

      }
      case 2 => {
        // DELETE
        dialog.dismiss()
        showIdeaDeleteDialog(idea)
      }

      case z => Log.d(APP_TAG, "showPrivateIdeaOptions builder didn't handle a click. Should never happen!")
    })



    builder.create().show()
  }




  private def showIdeaDeleteDialog(idea: Idea) {
    val title = "Delete " + idea.title + "?"
    val message = "Are you sure you want to delete this idea?"

    AlertDialogHelper.showYesCancelDialog(title, message, (dialog: DialogInterface, arg: Int) => {
      spawn {
        removeIdeaRequest(idea)
      }
      dialog.dismiss()
    })(getActivity)

    /*
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
    */
  }


  private def openSortOptions() {
    val builder = new AlertDialog.Builder(getActivity)

    builder.setTitle(R.string.sort_by)
    builder.setSingleChoiceItems(R.array.sort_options,
      getSavedSortStatus(), (dialog: DialogInterface, which: Int) => {
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
    var select = PrivateIdeaTableInfo.KEY_IS_IDEA_DELETED + "=0"

    AppSettings.PRIVATE_PARENT_IDEA match {
      case Some(idea) => select += " AND " + PrivateIdeaTableInfo.KEY_PARENT + "='" + idea.resource_uri + "'"
      case None => select += " AND " + PrivateIdeaTableInfo.KEY_PARENT + " IS NULL"
    }


    new CursorLoader(getActivity.getApplicationContext, PrivateIdeaProvider.CONTENT_URI, null, select, null, null)
  }

  def onLoadFinished(loader: Loader[Cursor], cursor: Cursor) {

    privateIdeas.clear()

    val ideas = getIdeasFromCursor(cursor)
    privateIdeas.addAll(ideas)

    sortIdeas(getSavedSortStatus())
  }

  def onLoaderReset(loader: Loader[Cursor]) {}

  //-------------------------------------------------------\\
  //------ END OF LOADER MANAGER CALLBACK METHODS ---------\\
  //-------------------------------------------------------\\


  // Key Listener that listens for back key and moves back within the private idea hierarchy
  def onKey(keyCode: Int, keyEvent: KeyEvent): Boolean =
    keyCode match {
      case KeyEvent.KEYCODE_BACK => {
        handleBackKey(keyEvent.getAction)
      }
      case _ => false
    }

  private def handleBackKey(keyAction: Int): Boolean = {
    AppSettings.PRIVATE_PARENT_IDEA match {
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
  //-------------- Synchronize Functions ------------------\\
  //-------------------------------------------------------\\
  private def startPrivateIdeaSync() {
    if (isServiceRunning(classOf[PrivateIdeaSyncService].getName)(getActivity)) {
      Toast.makeText(getActivity, "Already syncing personal ideas", Toast.LENGTH_SHORT).show()
    } else {

      Toast.makeText(getActivity, "Starting syncing of personal ideas...", Toast.LENGTH_SHORT).show()
      // Create the intent for the service
      val intent = new Intent(getActivity, classOf[PrivateIdeaSyncService])
      // Add receiver to the intent
      intent.putExtra(PRIVATE_IDEA_RESULT_RECEIVER, (resultCode: Int, resultData: Bundle) => {
        handler.post(refresh())
      })


      startService(intent) match {
        case None => if (App.DEBUG) Log.d(APP_TAG, "Couldn't start service in startPrivateIdeaSync because activity wasn't found")
        case Some(componentName) =>
      }

      // Start service
      //getActivity.startService(intent)

      //showIndeterminedProgress()
    }
  }


  private def startIdeaCreateService(idea: Idea) {
    // New ideas have id that is null
    val intent = new Intent(getActivity, classOf[IdeaCreateService])
    intent.putExtra("idea", idea.toJson)

    startService(intent)
  }

  private def startIdeaUpdateService(idea: Idea) {
    val intent = new Intent(getActivity, classOf[IdeaUpdateService])
    intent.putExtra("idea", idea.toJson)
    startService(intent)
  }

  private def startIdeaDeleteService(idea: Idea) {
    val intent = new Intent(getActivity, classOf[IdeaDeleteService])
    intent.putExtra("idea", idea.toJson)
    startService(intent)
  }

  private def syncIfNecessary() {
    if (App.DEBUG) Log.d(APP_TAG, "syncIfNecessary called")
    // Do this only if there is network connection
    if (isOnline(getActivity)) {
      // Get all ideas
      val ideasToUpload = getIdeasToUpload()
      val ideasToUpdate = getIdeasToUpdate()
      val ideasToDelete = getIdeasToDelete()

      if (App.DEBUG) {
        Log.d(APP_TAG, "There are " + ideasToUpdate.size() + " ideas to update")
        Log.d(APP_TAG, "There are " + ideasToUpload.size() + " ideas to upload")
        Log.d(APP_TAG, "There are " + ideasToDelete.size() + " ideas to delete")
      }

      for (idea <- ideasToUpload) {
        startIdeaCreateService(idea)
      }

      for (idea <- ideasToUpdate) {
        startIdeaUpdateService(idea)
      }

      for (idea <- ideasToDelete) {
        startIdeaDeleteService(idea)
      }


      if (App.DEBUG) Log.d(APP_TAG, "End of syncIfNecessary method")
      handler.post(refresh)
    }
  }

  private def getIdeasToUpdate(): util.ArrayList[Idea] = {
    val resolver = getActivity.getContentResolver

    val select = makeWhereClause(PrivateIdeaTableInfo.KEY_IS_IDEA_EDITED -> true, PrivateIdeaTableInfo.KEY_IS_IDEA_SYNCING -> false)
    //val select = PrivateIdeaTableInfo.KEY_IS_IDEA_EDITED + "=1 AND " + PrivateIdeaTableInfo.KEY_IS_IDEA_SYNCING + "=0"
    val cursor = resolver.query(PrivateIdeaProvider.CONTENT_URI, null, select, null, null)

    getIdeasFromCursor(cursor)
  }

  private def getIdeasToDelete(): util.ArrayList[Idea] = {
    val resolver = getActivity.getContentResolver

    val select = makeWhereClause(PrivateIdeaTableInfo.KEY_IS_IDEA_DELETED -> true, PrivateIdeaTableInfo.KEY_IS_IDEA_SYNCING -> false)

    //val select = PrivateIdeaTableInfo.KEY_IS_IDEA_DELETED + "=1 AND " + PrivateIdeaTableInfo.KEY_IS_IDEA_SYNCING + "=0"
    val cursor = resolver.query(PrivateIdeaProvider.CONTENT_URI, null, select, null, null)

    getIdeasFromCursor(cursor)
  }

  private def getIdeasToUpload(): util.ArrayList[Idea] = {
    val resolver = getActivity.getContentResolver

    val select = makeWhereClause(PrivateIdeaTableInfo.KEY_IS_IDEA_NEW -> true, PrivateIdeaTableInfo.KEY_IS_IDEA_SYNCING -> false)
    //val select = PrivateIdeaTableInfo.KEY_IS_IDEA_NEW + "=1 AND " + PrivateIdeaTableInfo.KEY_IS_IDEA_SYNCING + "=0"
    val cursor = resolver.query(PrivateIdeaProvider.CONTENT_URI, null, select, null, null)

    getIdeasFromCursor(cursor)
  }
}
