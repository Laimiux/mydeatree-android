package com.limeblast.mydeatree.activities

import android.os.{Handler, Bundle}
import _root_.android.widget.Toast
import _root_.android.content._

import _root_.android.preference.PreferenceManager
import android.util.Log
import com.actionbarsherlock.app.{ActionBar, SherlockFragmentActivity}
import com.actionbarsherlock.view.{Menu, MenuItem}
import com.actionbarsherlock.app.ActionBar.Tab

import android.net.Uri
import com.limeblast.androidhelpers.{WhereClauseModule, AndroidHelpers, ScalaHandler}
import com.limeblast.mydeatree._
import fragments.{FavoriteIdeaFragment, PrivateIdeaListFragment, PublicIdeaFragment}
import providers.RESTfulProvider
import com.limeblast.mydeatree.AppSettings._

import scala.Some
import com.limeblast.rest.{JsonModule}

import scala.concurrent.ops.spawn
import services.{FavoriteIdeaPostService, FavoriteIdeaDeleteService}
import android.database.Cursor

/**
 * Start activity that starts the app flow.
 */
class MainActivity extends SherlockFragmentActivity with TypedActivity with JsonModule with WhereClauseModule {

  // For tab names
  private val TAB_PRIVATE = "Personal"
  private val TAB_PUBLIC = "Public"
  private val TAB_FAVORITE = "Favorite"

  private var tabSelected: Int = 0

  private lazy val actionBar: ActionBar = getSupportActionBar

  private lazy val publicIdeaTab: Tab = actionBar.newTab().setText(TAB_PUBLIC)
  private lazy val privateIdeaTab: Tab = actionBar.newTab().setText(TAB_PRIVATE)
  private lazy val favoriteIdeaTab: Tab = actionBar.newTab().setText(TAB_FAVORITE)

  private lazy val mViewPager = findView(TR.pager)
  private lazy val mTabsAdapter = new TabsAdapter(this, actionBar, mViewPager)


  // Variables for MENUS
  private var actionMenu: Menu = _

  //-------------------------------------------------------\\
  //------------ ACTIVITY LIFECYCLE EVENTS ----------------\\
  //-------------------------------------------------------\\

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)

    setContentView(R.layout.main_layout)

    updateFromPreferences()

    // Creating actionBar
    actionBar.setDisplayShowHomeEnabled(false)
    actionBar.setDisplayShowTitleEnabled(false)
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)


    // Set the proper tab showing
    if (savedInstanceState != null) {
      tabSelected = savedInstanceState.getInt("selected_tab", 0)
    }


    getIntent.getStringExtra("idea") match {
      case null =>
      case ideaJson: String => {

        val idea = getMainObject(ideaJson, classOf[Idea])
        AppSettings.PRIVATE_PARENT_IDEA = Some(idea)

        tabSelected = 1

        getIntent().removeExtra("idea")
      }
    }
    //val haveNewPublicIdeas = intent.getBooleanExtra(HAS_NEW_PUBLIC_IDEAS, false)

    //  IdeaTableHelper.retrieveObject(this, RESTfulProvider.CONTENT_URI, classOf[Idea])
    mTabsAdapter.addTab(publicIdeaTab, classOf[PublicIdeaFragment], null)
    mTabsAdapter.addTab(privateIdeaTab, classOf[PrivateIdeaListFragment], null)
    mTabsAdapter.addTab(favoriteIdeaTab, classOf[FavoriteIdeaFragment], null)



    Log.d(APP_TAG, "Setting tab to " + tabSelected)
    getSupportActionBar.setSelectedNavigationItem(tabSelected)

    // Refresh resources
    spawn {
      refreshResources()
    }

    //
    //val provider = new PersonalIdeaProvider(getContentResolver)
    //provider.get_all()
  }

  override def onSaveInstanceState(outState: Bundle) {
    // Save tab position
    outState.putInt("selected_tab", getSupportActionBar.getSelectedNavigationIndex())
    super.onSaveInstanceState(outState)
  }

  def updateFromPreferences() {
    val context = getApplicationContext
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    App.USERNAME = prefs.getString(App.PREF_USERNAME, "")
    App.PASSWORD = prefs.getString(App.PREF_PASSWORD, "")

    LAST_PRIVATE_IDEAS_SYNCED = prefs.getString(PREF_LAST_PRIVATE_IDEAS_SYNCED, "")
  }


  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    actionMenu = menu

    val inflater = getSupportMenuInflater
    inflater.inflate(R.menu.main_menu, menu)

    super.onCreateOptionsMenu(menu)
  }


  /**
   * The menus get adjusted to the circumstances like user being logged in or not
   * @param menu Menu
   * @return
   */
  override def onPrepareOptionsMenu(menu: Menu): Boolean = {
    actionMenu = menu
    updateMenu()

    super.onPrepareOptionsMenu(menu)
  }


  override def onResume() {
    super.onResume()
    IS_MAIN_ACTIVITY_RUNNING = true
  }


  protected override def onPause() {
    super.onPause()
    IS_MAIN_ACTIVITY_RUNNING = false
  }

  //-------------------------------------------------------\\
  //------------------ SYNC FUNCTIONS ---------------------\\
  //-------------------------------------------------------\\
  private def refreshResources() {
    if (App.DEBUG) Log.d("MainActivity", "---------- STARTING REFRESH RESOURCES ------------")

    if (AndroidHelpers.isOnline(this)) {
      // Refresh favorite objects
      val favoritesToUpload = getFavoritesToUpload()
      val favoritesToDelete = getFavoritesToDelete()

      if (App.DEBUG) {
        Log.d("MainActivity", "There is " + favoritesToUpload.size + " favorites to upload")
        Log.d("MainActivity", "There is " + favoritesToDelete.size + " favorites to delete")
      }

      for (fav <- favoritesToUpload) {
        val intent = new Intent(this, classOf[FavoriteIdeaPostService])
        intent.putExtra("favorite_idea", convertObjectToJson(fav))

        startService(intent)
      }

      for (fav <- favoritesToDelete) {
        val intent = new Intent(this, classOf[FavoriteIdeaDeleteService])
        intent.putExtra("favorite_idea", convertObjectToJson(fav))

        startService(intent)
      }

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

    // Get cursor
    val cursor = App.FavoriteIdeaResource.Provider.getObjects(getContentResolver,
      null, select, null, null)

    val keyIdIndex = cursor.getColumnIndexOrThrow(FavoriteIdeaColumns.KEY_ID)
    val keyIdeaIndex = cursor.getColumnIndexOrThrow(FavoriteIdeaColumns.KEY_IDEA)
    val keyUriIndex = cursor.getColumnIndexOrThrow(FavoriteIdeaColumns.KEY_RESOURCE_URI)

    while(cursor.moveToNext()) {
      ideas = ideas :+ new FavoriteIdea(cursor.getString(keyIdIndex), cursor.getString(keyIdeaIndex), cursor.getString(keyUriIndex))
    }

    cursor.close()

    ideas
  }

  //-------------------------------------------------------\\
  //------------ VARIOUS ACTIVITY HANDLERS ----------------\\
  //-------------------------------------------------------\\
  override def onOptionsItemSelected(item: MenuItem): Boolean =
    item.getItemId match {
      case R.id.menu_item_logout => {
        logout()
        true
      }
      case R.id.menu_item_preferences => {
        val intent = new Intent(MainActivity.this, classOf[PreferencesActivity])
        startActivityForResult(intent, SHOW_PREFERENCES)
        true
      }
      case _ => super.onOptionsItemSelected(item)
    }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) =
    requestCode match {
      case SHOW_PREFERENCES => updateFromPreferences()
      case _ => super.onActivityResult(requestCode, resultCode, data)
    }


  //-------------------------------------------------------\\
  //---------------- LOG OUT FUNCTION ---------------------\\
  //-------------------------------------------------------\\
  def logout() {
    val context = getApplicationContext
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    // Remove all personal ideas
    val uri = Uri.withAppendedPath(RESTfulProvider.CONTENT_URI, "/" + App.USERNAME)
    getContentResolver.delete(uri, null, null)

    val editor = prefs.edit()
    editor.putString(App.PREF_USERNAME, "")
    editor.putString(App.PREF_PASSWORD, "")
    editor.commit()

    Toast.makeText(MainActivity.this, "See you later, " + App.USERNAME, Toast.LENGTH_SHORT).show()

    App.USERNAME = ""
    App.PASSWORD = ""

    // Move back to login screen
    val intent = new Intent()
    intent.setClass(MainActivity.this, classOf[LoginActivity])
    startActivity(intent)
    finish()
  }


  //-------------------------------------------------------\\
  //---------- HANDLE ACTIONBAR MENU PHASES ---------------\\
  //-------------------------------------------------------\\
  def updateMenu() =
    actionBar.getSelectedNavigationIndex match {
      case 0 => setToPublicMenu()
      case 1 => setToPrivateMenu()
      case 2 => setToFavoriteTabMenu()
    }

  def setToFavoriteTabMenu() = {
    actionMenu.setGroupVisible(R.id.menu_public_actions, false)
    actionMenu.setGroupVisible(R.id.menu_private_actions, false)

  }

  def setToPublicMenu() {
    actionMenu.setGroupEnabled(R.id.menu_public_actions, true)

    actionMenu.setGroupVisible(R.id.menu_public_actions, true)
    actionMenu.setGroupVisible(R.id.menu_private_actions, false)
  }

  def setToPrivateMenu() {
    if (actionMenu != null) {
      actionMenu.setGroupEnabled(R.id.menu_private_actions, true)
      actionMenu.setGroupVisible(R.id.menu_public_actions, false)
      actionMenu.setGroupVisible(R.id.menu_private_actions, true)
    }
  }

}