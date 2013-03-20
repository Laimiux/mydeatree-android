package com.limeblast.androidhelpers

import android.app.Activity
import android.os.Bundle

trait ForwardActivityTrait extends Activity {


  def getForwardActivityClass(): Class[_ <: Activity]

  override def onCreate(savedInstanceState: Bundle) {
    AndroidHelpers.changeActivity(this, getForwardActivityClass())

    super.onCreate(savedInstanceState)
  }

}
