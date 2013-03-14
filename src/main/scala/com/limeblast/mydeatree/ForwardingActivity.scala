package com.limeblast.mydeatree

import activities.{MainActivity, LoginActivity}
import com.actionbarsherlock.app.SherlockActivity
import com.limeblast.androidhelpers.ForwardActivityTrait
import android.app.Activity


/*
 * Activity forwards either to MainActivity
 * or LoginActivity, depending if user
 * is authenticated.
 */
class ForwardingActivity extends SherlockActivity with ForwardActivityTrait {

  def getForwardActivityClass(): Class[_ <: Activity] =
    App.isLoggedIn(this) match {
      case true => classOf[MainActivity]
      case false => classOf[LoginActivity]
    }
}
