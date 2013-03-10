package com.limeblast.mydeatree.activities

import android.os.Bundle
import android.preference.PreferenceActivity
import com.limeblast.mydeatree.{R, TypedActivity}


/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 1/16/13
 * Time: 9:52 AM
 * To change this template use File | Settings | File Templates.
 */
class PreferencesActivity extends PreferenceActivity with TypedActivity {

  var updateFreq: Int = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    addPreferencesFromResource(R.xml.userpreferences)

  }


}
