package com.limeblast.mydeatree.fragments

import com.actionbarsherlock.app.SherlockFragment
import android.support.v4.app.{Fragment, LoaderManager}
import android.database.Cursor
import android.support.v4.content.{CursorLoader, Loader}
import android.os.{Handler, Bundle}
import java.util.{ArrayList, List => JList }
import android.view.{KeyEvent, View, ViewGroup, LayoutInflater}
import android.widget._


import android.util.Log
import com.actionbarsherlock.view.MenuItem
import android.content
import android.app.AlertDialog
import content.{Intent, DialogInterface}

import com.limeblast.mydeatree._
import activities.{IdeaEditActivity, NewIdeaActivity}
import com.limeblast.mydeatree.AppSettings._
import providers.{PrivateIdeaProvider, PublicIdeaProvider}
import services.PublicIdeaSyncService
import com.limeblast.androidhelpers.{ScalifiedAndroid, AlertDialogHelper, ScalifiedTraitModule}
import android.app.AlertDialog.Builder
import annotation.switch
import storage.{PrivateIdeaTableInfo, PublicIdeaTableInfo}
import com.limeblast.rest.JsonModule
import scala.Some

import scala.collection.JavaConversions._

class PublicIdeaFragment extends SherlockFragment
with HasParentState[PublicIdea]
with LoaderManager.LoaderCallbacks[Cursor] with ScalifiedTraitModule with JsonModule {
  private val APP_TAG = "PUBLIC_IDEA_FRAGMENT"


  private lazy val handler: Handler = new Handler()

  private var publicIdeaListView: ListView = _
  //var aa: ArrayAdapter[PublicIdea] = _
  var aa: ArrayAdapter[ListItem] = _

  // Defines how to sort ideas
  private var sort_by = 0




  //-------------------------------------------------------\\
  //-------- USED FOR SECTIONING IMPLEMENTATION -----------\\
  //-------------------------------------------------------\\
  sealed trait ListItem

  class SectionItem(val dividerText: String) extends ListItem
  //class EntryItem(val item: PublicIdea) extends ListItem


  private var ideas = List[PublicIdea]()


  //private val publicIdeas: ArrayList[PublicIdea] = new ArrayList()
  private var listItems = new ArrayList[ListItem]()


  //-------------------------------------------------------\\
  //----------- PARENT STATE IMPLEMENTATION ---------------\\
  //-------------------------------------------------------\\
  var parentObject: Option[PublicIdea] = None

  override def setParent(parent: Option[PublicIdea]) {
    super.setParent(parent)

    refresh()

    if (App.DEBUG) Log.d(APP_TAG, "setParent method call has been handled.")
  }


  /*
  private def moveToPreviousParent() {
    parentObject match {
      case Some(idea) => moveToPreviousParent(idea)
      case _ =>
    }
  }
  */


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


    //aa = new PublicIdeaListAdapter(this, layoutID, publicIdeas)
    aa = new EntryArrayAdapter(this, layoutID, listItems)

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

  def sortIdeas = {
    sort_by match {
      case 0 => ideas = ideas.sortWith(IdeaSortStrategy.compareByModifiedDate)
      case 1 => ideas = ideas.sortWith(IdeaSortStrategy.compareByCreatedDate)
      case 2 => ideas = ideas.sortWith(_.title.toUpperCase < _.title.toUpperCase)
    }


    listItems.clear()

    var previousOption: String = null

    for(idea <- ideas) {
      sort_by match {
        case 2 => {
          val firstLetter = idea.title.take(1).toUpperCase
          if(firstLetter != previousOption) {
            listItems.add(new SectionItem(firstLetter))
            previousOption = firstLetter
          }
        }
        case z =>
      }
      idea match {
        case item: ListItem => listItems.add(item)
      }
    }

    handler.post(aa.notifyDataSetChanged())
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
  private def showDeleteIdeaDialog(pIdea: PublicIdea) = {
    val title = "Delete " + pIdea.title + "?"
    val message = "Are you sure you want to delete this idea?"

    AlertDialogHelper.showYesCancelDialog(title, message, (dialog, arg) => {
      shortToast("not implemented yet. Should open delete idea dialog")(getActivity)
    })(getActivity)
  }

  private def showMakeIdeaPrivateDialog(idea: PublicIdea) {
    val title = "Make " + idea.title + " private?"
    val message = "Are you sure you want to make this idea private?"

    AlertDialogHelper.showYesCancelDialog(title, message, (dialog, arg) => {
      shortToast("not implemented yet. Should make idea private.")(getActivity)
    })(getActivity)
  }




  private def showSortOptions() {
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
      case 0 => shortToast("not implemented yet. Should open new children idea activity.")(getActivity)
      case 1 => editIdea(pIdea)
      case 2 => showDeleteIdeaDialog(pIdea)
      case 3 => showMakeIdeaPrivateDialog(pIdea)
      case 4 => shareIdea(pIdea)
      case z => getActivity.shortToast("Ain't shit handled at " + z)
    })

    builder.show()
  }

  def openPublicIdeaOptions(pIdea: PublicIdea) {
    if (App.DEBUG) Log.d(APP_TAG, "Creating public idea option menu.")

    val builder = new Builder(getActivity)
    builder.setTitle(pIdea.title + " by " + pIdea.owner.username)

    builder.setItems(R.array.public_idea_options, (dialog: DialogInterface, which: Int) => (which: Int@switch) match {
      case 0 => shareIdea(pIdea)
      case 1 => getActivity.shortToast("Favorite clicked")
      case z => getActivity.shortToast("unhandled " + z)
    })


    builder.show()
  }

  //-------------------------------------------------------\\
  //--------- VARIOUS FRAGMENT HELPER FUNCTIONS -----------\\
  //-------------------------------------------------------\\


  //-------------------------------------------------------\\
  //------------ VARIOUS FRAGMENT ACTIONS -----------------\\
  //-------------------------------------------------------\\

  private def editIdea(idea: PublicIdea) {
    val id = idea.id

    val intent = new Intent(getActivity, classOf[IdeaEditActivity])
    intent.putExtra("idea_id", id)
    startActivity(intent)
  }


  private def shareIdea(idea: PublicIdea) {
    val sharingIntent = new Intent(Intent.ACTION_SEND)
    sharingIntent.setType("text/plain")
    sharingIntent.putExtra(Intent.EXTRA_TEXT, "https://mydeatree.appspot.com/idea/" + idea.id + "/")
    startActivity(Intent.createChooser(sharingIntent, "Share with "))
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
      showSortOptions()
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




    ideas = List()





    while (cursor.moveToNext()) {
      val idea = new PublicIdea(cursor.getString(keyTitleIndex),
        cursor.getString(keyTextIndex),
        cursor.getString(keyIdIndex),
        cursor.getString(keyParentIndex),
        cursor.getString(keyCreatedDateIndex),
        cursor.getString(keyModifiedDateIndex),
        cursor.getString(keyResourceUriIndex),
        new Owner(cursor.getString(keyOwnerIndex)),
        cursor.getInt(keyChildrenIdea)) with ListItem

      ideas = ideas :+ idea

    }

    if (App.DEBUG) Log.d(APP_TAG, "Retrieved " + ideas.length + " public ideas")

    sortIdeas
  }

  def onLoaderReset(loader: Loader[Cursor]) {}

  //-------------------------------------------------------\\
  //------ END OF LOADER MANAGER CALLBACK METHODS ---------\\
  //-------------------------------------------------------\\



  private class EntryArrayAdapter[T <: Fragment with HasParentState[PublicIdea]](val fragment: T, resourceId: Int, objects: JList[ListItem])
    extends ArrayAdapter(fragment.getActivity, resourceId, objects) with FavoriteIdeaProviderModule with ScalifiedAndroid {

    import com.limeblast.mydeatree.storage.FavoriteIdeaColumns

    val context = fragment.getActivity


    override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
      val inflater: LayoutInflater = LayoutInflater.from(context)
      val cView = inflater.inflate(resourceId, null).asInstanceOf[LinearLayout]

      //cView.setLongClickable(true)

      getItem(position) match {
        case a: SectionItem => {
          val separator = cView.findViewById(R.id.separator).asInstanceOf[TextView]
          separator.setText(a.dividerText)

          val ideaHolder = cView.findViewById(R.id.public_idea_entry_holder)
          ideaHolder.setVisibility(View.GONE)
        }
        case idea: PublicIdea => {
          val separator = cView.findViewById(R.id.separator)
          separator.setVisibility(View.GONE)


          val txtTitle = cView.findViewById(R.id.idea_title).asInstanceOf[TextView]
          txtTitle.setText(idea.title)

          val txtOwner = cView.findViewById(R.id.idea_owner).asInstanceOf[TextView]
          txtOwner.setText(" by " + idea.owner.username)

          val txtText = cView.findViewById(R.id.idea_text).asInstanceOf[TextView]
          txtText.setText(idea.text)

          val dateText = cView.findViewById(R.id.public_idea_date).asInstanceOf[TextView]
          val date = Helpers.stringToDate(idea.modified_date)
          dateText.setText(Helpers.formatDate(date))



          // Remove fav/share buttons.

          val viewBtns = cView.findViewById(R.id.public_idea_button_holder).asInstanceOf[LinearLayout]
          viewBtns.setVisibility(View.GONE)

          // Checks if idea is favorited
          var favorited: Boolean = isFavorite(idea)

          val moreIdeasButton = cView.findViewById(R.id.public_more_ideas_button).asInstanceOf[ImageButton]


          if (idea.children_count > 0) {
            moreIdeasButton.onClick({
              fragment.setParent(Some(idea))
            })
          } else {
            moreIdeasButton.setVisibility(View.GONE)
          }

        }

      }

      return cView
    }

    private def isFavorite(idea: PublicIdea): Boolean = {

      val resolver = context.getContentResolver
      val select = makeWhereClause(FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri, FavoriteIdeaColumns.KEY_IS_DELETED -> false)

      val cursor: Cursor = getObjects(resolver, null, select, null, null)

      val isIdeaFavorited = cursor.getCount() > 0
      cursor.close()

      isIdeaFavorited

    }

    /**
     * Save to database a favorite idea
     * @param idea PublicIdea that is being favorited
     */
    private def setToFavorite(idea: PublicIdea) {
      val resolver = context.getContentResolver
      val select = makeWhereClause(FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri, FavoriteIdeaColumns.KEY_IS_DELETED -> true)
      val cursor = getObjects(resolver, null, select, null, null)


      val isNew = cursor.getCount > 0

      cursor.close()

      if (isNew)  // Update the favorite
        updateObjects(resolver, (FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri), null, Map(FavoriteIdeaColumns.KEY_IS_DELETED -> false))
      else  // Insert a new favorite
        insertObject(resolver)(FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri, FavoriteIdeaColumns.KEY_IS_NEW -> true)
    }


    /**
     * Checks if favorite is on server than marks it for deletion,
     * otherwise completely removes it from database
     * @param idea PublicIdea to be removed from favorites
     */
    private def removeFavorite(idea: PublicIdea) {
      val resolver = context.getContentResolver
      val select = makeWhereClause(FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri, FavoriteIdeaColumns.KEY_IS_NEW -> true)
      val cursor = getObjects(resolver, null, select, null, null)

      val isOnServer = cursor.getCount == 0

      cursor.close()

      if (isOnServer) {
        updateObjects(resolver, (FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri), null, Map(FavoriteIdeaColumns.KEY_IS_DELETED -> true))
      } else {
        deleteObjects(resolver, (FavoriteIdeaColumns.KEY_IDEA -> idea.resource_uri), null)
      }
    }
  }

}
