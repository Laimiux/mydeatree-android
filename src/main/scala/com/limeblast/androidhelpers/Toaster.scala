package com.limeblast.androidhelpers

import android.widget.Toast
import android.content.Context

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 3/7/13
 * Time: 11:13 AM
 * To change this template use File | Settings | File Templates.
 */
object Toaster {
  def showToast(ctx: Context, msg: String, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(ctx,msg, duration).show()

}
