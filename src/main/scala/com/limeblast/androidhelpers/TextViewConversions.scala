package com.limeblast.androidhelpers

import android.widget.TextView
import android.view.View

sealed trait HasLongClickListener[T <: TextView] {
  def self: T

}

trait TextViewConversions {

}
