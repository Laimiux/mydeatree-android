package com.limeblast.androidhelpers

import android.view.LayoutInflater
import android.content.Context

trait Inflater {
  def context: Context
  val inflater: LayoutInflater = LayoutInflater.from(context)
}
