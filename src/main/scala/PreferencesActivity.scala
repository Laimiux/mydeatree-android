package com.limeblast.mydeatree

import android.app.Activity
import android.os.Bundle
import android.content.SharedPreferences
import android.widget.{ArrayAdapter, Spinner}
import android.preference.{PreferenceActivity, PreferenceManager}

import AppSettings._
import android.view.View
import android.view.View.OnClickListener

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
