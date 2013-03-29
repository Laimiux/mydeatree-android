package com.limeblast.androidhelpers

import android.widget.TextView
import android.view.View
import android.view.View.OnClickListener

sealed trait HasLongClickListener[T <: TextView] {
  def self: T

}

sealed trait HasOnClickListener[T <: TextView] {
  def self: T

  def onClick[F](f: (View) => F) {
    self.setOnClickListener(new OnClickListener {
      def onClick(v: View) = f(v)

    })
  }

  def onClick[F](f: => F) {
    self.setOnClickListener(new OnClickListener {
      def onClick(v: View) {
        f
      }
    })
  }
}

trait TextViewConversions {
  implicit def textViewToHasOnClick[T <: TextView](tv: T): HasOnClickListener[T] = new HasOnClickListener[T] {
    val self = tv
  }
}
