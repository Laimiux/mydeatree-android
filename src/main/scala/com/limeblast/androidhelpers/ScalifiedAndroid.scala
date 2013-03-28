package com.limeblast.androidhelpers

import android.widget.TextView
import android.view.View
import android.view.View.OnClickListener
import android.os.{ResultReceiver, Bundle, Handler}
import android.content.Intent

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 3/28/13
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
sealed trait HasOnClick[T <: TextView] {
  def self: T

  def onClick(f: (View) => Unit) {
    self.setOnClickListener(new OnClickListener {
      def onClick(v: View) = f(v)

    })
  }
}

sealed trait FunctionToRunnable {
  implicit def toRunnable[F](f: => F): Runnable =
    new Runnable() {
      def run() = f
    }
}

sealed trait FunctionToResultReceiver {
  implicit def functionToResultReceicer[F](f: (Int, Bundle) => F): ResultReceiver = new ResultReceiver(null) {
    override def onReceiveResult(resultCode: Int, resultData: Bundle) {
      f(resultCode, resultData)
    }
  }
}

sealed trait HandlerPostTakesFunction extends FunctionToRunnable {
  def handler: Handler

  def post[F](f: => F) {
    handler.post(f)
  }
}

sealed trait IntentFunctionToResultReceiver extends FunctionToResultReceiver{
  def self: Intent

  def putExtra[F](name: String, f: (Int, Bundle) => F) {
    self.putExtra(name, f)
  }
}


/**
 * Lots of implicit object conversions to give more
 * functionality to certain android classes.
 */
object ScalifiedAndroid {
  implicit def textViewToHasOnClick[T <: TextView](tv: T): HasOnClick[T] = new HasOnClick[T] {
    val self = tv
  }

  implicit def handlerToAcceptFunction(tv: Handler): HandlerPostTakesFunction = new HandlerPostTakesFunction {
    val handler: Handler = tv
  }


  implicit def intentToAcceptFuncAsResultReceiver(intent: Intent): IntentFunctionToResultReceiver = new IntentFunctionToResultReceiver {
    val self: Intent = intent
  }


}
