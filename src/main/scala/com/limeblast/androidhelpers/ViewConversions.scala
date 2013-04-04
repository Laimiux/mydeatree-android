package com.limeblast.androidhelpers

import android.view.{KeyEvent, View}
import android.view.View.{OnClickListener, OnKeyListener}
import android.widget.TextView


sealed class ExpandedSetOnKeyListenerModule(val vw: View) {

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

sealed class HasOnClickListener[T <: View](val tv: T) {

  def onClick[F](f: (View) => F) {
    tv.setOnClickListener(new OnClickListener {
      def onClick(v: View) = f(v)

    })
  }

  def onClick[F](f: => F) {
    tv.setOnClickListener(new OnClickListener {
      def onClick(v: View) {
        f
      }
    })
  }
}

trait ViewConversions {
  implicit def giveViewExpandedOnKeyListenedModule[T <: View](view: T): ExpandedSetOnKeyListenerModule = new ExpandedSetOnKeyListenerModule(view)

  implicit def viewToHasOnClick[T <: View](tv: T): HasOnClickListener[T] = new HasOnClickListener[T](tv)
}
