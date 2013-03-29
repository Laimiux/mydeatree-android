package com.limeblast.androidhelpers

import android.view.{KeyEvent, View}
import android.view.View.OnKeyListener


sealed trait ExpandedSetOnKeyListenerModule {
  def vw: View

  def setOnKeyListener[F](f: (View, Int, KeyEvent) => Boolean) = vw.setOnKeyListener(new OnKeyListener {
    def onKey(v: View, keyCode: Int, event: KeyEvent): Boolean = {
      f(v, keyCode, event)
    }
  })


  def setOnKeyListener[F](f: (Int, KeyEvent) => Boolean) = vw.setOnKeyListener(new OnKeyListener {
    def onKey(v: View, keyCode: Int, event: KeyEvent): Boolean = {
      f(keyCode, event)
    }
  })
}

trait ViewConversions {
  implicit def giveViewExpandedOnKeyListenedModule[T <: View](view: T): ExpandedSetOnKeyListenerModule = new ExpandedSetOnKeyListenerModule {
    val vw: View = view
  }
}
