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
import com.limeblast.androidhelpers.{ScalifiedActivity, WhereClauseModule}
import com.limeblast.mydeatree._
import fragments.{FavoriteIdeaFragment, PrivateIdeaListFragment, PublicIdeaFragment}
import providers.RESTfulProvider
import com.limeblast.mydeatree.AppSettings._

import scala.Some
import com.limeblast.rest.{JsonModule}

import com.limeblast.androidhelpers.ScalifiedAndroid._


/**
 * Start activity that starts the app flow.
 */
class MainActivity extends SherlockFragmentActivity with ScalifiedActivity with TypedActivity with JsonModule with WhereClauseModule {

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
  //private var actionMenu: Menu = _

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

    //
    //val provider = new PersonalIdeaProvider(getContentResolver)
    //provider.get_all()
  }

  override def onSaveInstanceState(outState: Bundle) {
    // Save tab position
    outState.putInt("selected_tab", getSupportActionBar.getSelectedNavigationIndex())
    super.onSaveInstanceState(outState)
  }

  def updateFromPreferences() = {
    val preferences = getDefaultPreferences()
    App.USERNAME = preferences.getString(App.PREF_USERNAME, "")
    App.PASSWORD = preferences.getString(App.PREF_PASSWORD, "")

    LAST_PRIVATE_IDEAS_SYNCED = preferences.getString(PREF_LAST_PRIVATE_IDEAS_SYNCED, "")
  }

  /*
    getDefaultPreferences() match {
      case Some(preferences) => {

      }
      case None => if (App.DEBUG) Log.d(APP_TAG, "updateFromPreferences didn't get preferences. We are in tons of pain now!")
    }
    */




  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    //actionMenu = menu

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
   // actionMenu = menu
    updateMenu(menu)
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
  //------------ VARIOUS ACTIVITY HANDLERS ----------------\\
  //-------------------------------------------------------\\
  override def onOptionsItemSelected(item: MenuItem): Boolean =
    item.getItemId match {
      case R.id.menu_item_logout => {
        logout()
        true
      }
      case R.id.menu_item_preferences => {
        openPreferenceActivity()
        true
      }
      case _ => super.onOptionsItemSelected(item)
    }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) =
    requestCode match {
      case SHOW_PREFERENCES => updateFromPreferences()
      case _ => super.onActivityResult(requestCode, resultCode, data)
    }


  private def openPreferenceActivity() {
    startActivityForResult(classOf[PreferencesActivity], SHOW_PREFERENCES)
  }


  //---------------------------------   ----------------------\\
  //---------------- LOG OUT FUNCTION ---------------------\\
  //-------------------------------------------------------\\
  private def clearUserInfoFromPreferences() {
    val editor = getDefaultPreferences().edit()
    editor.putString(App.PREF_USERNAME, "")
    editor.putString(App.PREF_PASSWORD, "")
    editor.commit()
  }

  private def removeUserDatabaseInfo() {
    // Remove all personal ideas
    val uri = Uri.withAppendedPath(RESTfulProvider.CONTENT_URI, "/" + App.USERNAME)
    getContentResolver.delete(uri, null, null)
  }

  private def clearUserLoginVariables() {
    App.USERNAME = ""
    App.PASSWORD = ""
  }

  private def moveBackToLogin() {
    startActivity(classOf[LoginActivity])
    finish()
  }

  /**
   * Logs the current user out.
   */
  def logout() {
    removeUserDatabaseInfo()
    clearUserInfoFromPreferences()

    Toast.makeText(MainActivity.this, "See you later, " + App.USERNAME, Toast.LENGTH_SHORT).show()

    clearUserLoginVariables()

    moveBackToLogin()
  }


  //-------------------------------------------------------\\
  //---------- HANDLE ACTIONBAR MENU PHASES ---------------\\
  //-------------------------------------------------------\\
  def updateMenu(menu: Menu) =
    actionBar.getSelectedNavigationIndex match {
      case 0 => setToPublicMenu(menu)
      case 1 => setToPrivateMenu(menu)
      case 2 => setToFavoriteTabMenu(menu)
    }

  private def setToFavoriteTabMenu(menu: Menu) = {
    menu.setGroupVisible(R.id.menu_public_actions, false)
    menu.setGroupVisible(R.id.menu_private_actions, false)

  }

  private def setToPublicMenu(menu: Menu) {
    menu.setGroupEnabled(R.id.menu_public_actions, true)

    menu.setGroupVisible(R.id.menu_public_actions, true)
    menu.setGroupVisible(R.id.menu_private_actions, false)
  }

  private def setToPrivateMenu(menu: Menu) {
    if (menu != null) {
      menu.setGroupEnabled(R.id.menu_private_actions, true)
      menu.setGroupVisible(R.id.menu_public_actions, false)
      menu.setGroupVisible(R.id.menu_private_actions, true)
    }
  }

}