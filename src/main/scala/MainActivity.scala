package com.limeblast.mydeatree

import _root_.android.os.Bundle
import com.actionbarsherlock.app.SherlockFragmentActivity

class MainActivity extends SherlockFragmentActivity with TypedActivity {
  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)

    findView(TR.textview).setText("hello, world!")
  }
}
