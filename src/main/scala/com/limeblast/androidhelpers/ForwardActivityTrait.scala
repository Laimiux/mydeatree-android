package com.limeblast.androidhelpers

import android.app.Activity
import android.os.Bundle

trait ForwardActivityTrait extends Activity with ScalifiedActivity {


  def getForwardActivityClass(): Class[_ <: Activity]

  override def onCreate(savedInstanceState: Bundle) {
    startActivity(getForwardActivityClass())
    finish()

    super.onCreate(savedInstanceState)
  }

}
