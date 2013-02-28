package com.limeblast.mydeatree

import com.actionbarsherlock.app.SherlockActivity
import android.os.Bundle
import android.content.Intent

class ForwardingActivity extends SherlockActivity {

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
}
