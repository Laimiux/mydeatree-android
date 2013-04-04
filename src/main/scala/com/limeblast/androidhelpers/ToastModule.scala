package com.limeblast.androidhelpers

import android.content.Context
import android.widget.Toast

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 4/3/13
 * Time: 3:57 PM
 * To change this template use File | Settings | File Templates.
 */
trait ToastModule {

  def shortToast[T <: Context](msg: String)(implicit context: T) =
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

  def longToast[T <: Context](msg: String)(implicit context: T) =
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

}
