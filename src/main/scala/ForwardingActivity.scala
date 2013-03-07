package com.limeblast.mydeatree

import com.actionbarsherlock.app.SherlockActivity
import com.limeblast.androidhelpers.TwoWayForwardTrait


/*
 * Activity forwards either to MainActivity
 * or LoginActivity, depending if user
 * is authenticated.
 */
class ForwardingActivity extends SherlockActivity with TwoWayForwardTrait {
  def switch: Boolean = AppSettings.isLoggedIn(this)
  def falseActivity = classOf[LoginActivity]
  def trueActivity = classOf[MainActivity]

}
