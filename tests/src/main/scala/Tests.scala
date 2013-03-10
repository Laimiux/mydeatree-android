package com.limeblast.mydeatree.tests

import com.limeblast.mydeatree._
import activities.ForwardingActivity
import junit.framework.Assert._
import _root_.android.test.AndroidTestCase
import _root_.android.test.ActivityInstrumentationTestCase2
import com.actionbarsherlock.app.ActionBar

class AndroidTests extends AndroidTestCase {
  def testPackageIsCorrect() {
    assertEquals("com.limeblast.mydeatree", getContext.getPackageName)
  }
}

class ActivityTests extends ActivityInstrumentationTestCase2(classOf[ForwardingActivity]) {
   def testHelloWorldIsShown() {
     val activity = getActivity
     assertEquals(activity.getSupportActionBar.getNavigationMode, ActionBar.NAVIGATION_MODE_TABS)
     // val textview = activity.findView(TR.textview)
      //assertEquals(textview.getText, "hello, world!")
    }
}
