package com.limeblast.androidhelpers

import android.view.{View, ViewGroup}
import android.widget.AdapterView

sealed trait ImplicitFunctionToItemLongListener {
  implicit def functionToLongListener[F](f: (AdapterView[_],View, Int, Long) => Boolean): AdapterView.OnItemLongClickListener =
    new AdapterView.OnItemLongClickListener {
      def onItemLongClick(parent: AdapterView[_], view: View, position: Int, id: Long): Boolean = f(parent, view, position, id)

    }
}


sealed class HasItemLongClickListener(val adapter: AdapterView[_]) extends ImplicitFunctionToItemLongListener {
  //def self: AdapterView[_]

  def onItemLongClick(f: (AdapterView[_],View, Int, Long) => Boolean): Unit = adapter.setOnItemLongClickListener(f)
}

trait AdapterViewConversions {
  implicit def adapterViewHasItemLongClickListener(adapter: AdapterView[_]): HasItemLongClickListener = new HasItemLongClickListener(adapter)
}
