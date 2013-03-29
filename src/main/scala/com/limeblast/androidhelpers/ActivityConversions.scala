package com.limeblast.androidhelpers

import android.app.Activity
import android.widget.Toast
import android.content.Context

sealed trait ShowToastModuleWithContext[T <: Context] {
  def ctx: T

  def shortToast(msg: String) = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()

  def longToast(msg: String) = Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()

}

trait ActivityConversions {
  implicit def activityToShowToastModule[T <: Activity](activity: T) = new ShowToastModuleWithContext[T] {
    val ctx = activity
  }
}
