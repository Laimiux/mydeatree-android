package com.limeblast.androidhelpers

import android.os.{ResultReceiver, Bundle, Handler}
import android.content.Intent


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

trait ScalifiedAndroid extends AdapterViewConversions with AlertDialogBuilderConversions with PreferenceConversions
with ActivityConversions with ViewConversions with ToastModule {


  implicit def handlerToAcceptFunction(tv: Handler): HandlerPostTakesFunction = new HandlerPostTakesFunction {
    val handler: Handler = tv
  }


  implicit def intentToAcceptFuncAsResultReceiver(intent: Intent): IntentFunctionToResultReceiver = new IntentFunctionToResultReceiver {
    val self: Intent = intent
  }


}
