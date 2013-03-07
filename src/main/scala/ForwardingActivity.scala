package com.limeblast.mydeatree

import com.actionbarsherlock.app.SherlockActivity
import com.limeblast.androidhelpers.TwoWayForwardTrait

class ForwardingActivity extends SherlockActivity with TwoWayForwardTrait {


  def switch: Boolean = AppSettings.isLoggedIn(this)
  def falseActivity = classOf[LoginActivity]
  def trueActivity = classOf[MainActivity]

  /*
  override def onCreate(savedInstanceState: Bundle) {
    if (AppSettings.isLoggedIn(this)) {
      val intent = new Intent()
      intent.setClass(ForwardingActivity.this, classOf[MainActivity])
      startActivity(intent)
      finish()
    } else {

      val intent = new Intent()
      intent.setClass(ForwardingActivity.this, classOf[LoginActivity])
      startActivity(intent)
      finish()
    }

    super.onCreate(savedInstanceState)
  }
  */
}
