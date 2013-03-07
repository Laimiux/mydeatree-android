package com.limeblast.mydeatree

import android.os.{Handler, Bundle}
import _root_.android.widget.{Toast}
import _root_.android.content._

import AppSettings._
import _root_.android.preference.PreferenceManager
import android.util.{Log}
import com.actionbarsherlock.app.{ActionBar, SherlockFragmentActivity}
import com.actionbarsherlock.view.{Menu, MenuItem}
import com.actionbarsherlock.app.ActionBar.Tab

import scala.collection.JavaConversions._
import android.net.Uri
import com.limeblast.androidhelpers.{PersonalIdeaProvider, ScalaHandler}

/**
 * Start activity that starts the app flow.
 */
class MainActivity extends SherlockFragmentActivity with TypedActivity {

  val handler: Handler = new ScalaHandler()

  // For tab names
  private val TAB_PRIVATE = "Personal"
  private val TAB_PUBLIC = "Public"

  private var tabSelected: Int = 0

  private lazy val actionBar: ActionBar = getSupportActionBar

  private lazy val publicIdeaTab: Tab = actionBar.newTab().setText(TAB_PUBLIC)
  private lazy val privateIdeaTab: Tab = actionBar.newTab().setText(TAB_PRIVATE)

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


    val intent = getIntent()

    getIntent.getStringExtra("idea") match {
      case null =>
      case ideaJson: String => {

        val idea = JsonWrapper.getMainObject(ideaJson, classOf[Idea])
        AppSettings.PRIVATE_PARENT_IDEA = Some(idea)

        tabSelected = 1

        getIntent().removeExtra("idea")
      }
    }
    //val haveNewPublicIdeas = intent.getBooleanExtra(HAS_NEW_PUBLIC_IDEAS, false)

    //  IdeaTableHelper.retrieveObject(this, RESTfulProvider.CONTENT_URI, classOf[Idea])
    mTabsAdapter.addTab(publicIdeaTab, classOf[PublicIdeaFragment], null)
    mTabsAdapter.addTab(privateIdeaTab, classOf[PrivateIdeaListFragment], null)



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

  def updateFromPreferences() {
    val context = getApplicationContext
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    USERNAME = prefs.getString(PREF_USERNAME, "")
    PASSWORD = prefs.getString(PREF_PASSWORD, "")

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
    if (!isLoggedIn) {
      menu.setGroupVisible(R.id.menu_loggedin, false)
      menu.setGroupVisible(R.id.menu_loggedout, true)
    } else {
      menu.setGroupVisible(R.id.menu_loggedout, false)
      menu.setGroupVisible(R.id.menu_loggedin, true)
    }

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
    val uri = Uri.withAppendedPath(RESTfulProvider.CONTENT_URI, "/" + USERNAME)
    getContentResolver.delete(uri, null, null)

    val editor = prefs.edit()
    editor.putString(PREF_USERNAME, "")
    editor.putString(PREF_PASSWORD, "")
    editor.commit()

    Toast.makeText(MainActivity.this, "See you later, " + USERNAME, Toast.LENGTH_SHORT).show()

    USERNAME = ""
    PASSWORD = ""

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
      if (!USERNAME.equals("")) {
        actionMenu.setGroupVisible(R.id.menu_private_actions, true)
      } else {
        actionMenu.setGroupVisible(R.id.menu_private_actions, false)
      }

    }
  }

}