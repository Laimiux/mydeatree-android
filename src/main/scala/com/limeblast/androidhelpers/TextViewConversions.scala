package com.limeblast.androidhelpers

import android.widget.TextView
import android.view.View
import android.view.View.OnClickListener

sealed trait HasLongClickListener[T <: TextView] {
  def self: T

}

sealed class HasOnClickListener[T <: TextView](val tv: T) {

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

trait TextViewConversions {
  implicit def textViewToHasOnClick[T <: TextView](tv: T): HasOnClickListener[T] = new HasOnClickListener[T](tv)
}
