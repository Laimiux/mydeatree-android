package com.limeblast.androidhelpers

import android.app.Activity
import android.os.Bundle

trait TwoWayForwardTrait extends Activity {
  def switch: Boolean

  def falseActivity: Class[_ <: Activity]

  def trueActivity: Class[_ <: Activity]

  override def onCreate(savedInstanceState: Bundle) {
    switch match {
      case true => {
        AndroidHelpers.changeActivity(this, trueActivity)
      }
      case false => {
        AndroidHelpers.changeActivity(this, falseActivity)
      }
    }

    super.onCreate(savedInstanceState)
  }

}
