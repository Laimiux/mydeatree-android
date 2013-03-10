package com.limeblast.androidhelpers

import android.view.View
import android.os.{Bundle, ResultReceiver}
import android.widget.AdapterView
import android.content.DialogInterface


object AndroidImplicits {


  implicit def toListener[F](f: View => F) =
    new View.OnClickListener {
      def onClick(view: View) {
        f(view)
      }
    }

  implicit def functionToLongListener[F](f: (AdapterView[_],View, Int, Long) => Boolean): AdapterView.OnItemLongClickListener =
    new AdapterView.OnItemLongClickListener {
      def onItemLongClick(parent: AdapterView[_], view: View, position: Int, id: Long): Boolean = {
        f(parent, view, position, id)
      }
    }

  implicit def toRunnable[F](f: => F): Runnable =
    new Runnable() {
      def run() = f
    }

  implicit def functionToResultReceicer[F](f:(Int,Bundle) => F): ResultReceiver = new ResultReceiver(null) {
    override def onReceiveResult(resultCode: Int, resultData: Bundle) {
      f(resultCode, resultData)
    }
  }


  implicit def functionToDialogOnClickListener[F](f:(DialogInterface, Int) => F): DialogInterface.OnClickListener =
    new DialogInterface.OnClickListener {
      def onClick(dialog: DialogInterface, position: Int) {
        f(dialog, position)
      }
    }
}
