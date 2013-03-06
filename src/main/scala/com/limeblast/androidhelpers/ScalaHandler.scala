package com.limeblast.androidhelpers

import android.os.Handler


class ScalaHandler extends Handler {
  def toRunnable[F](f: => F): Runnable =
    new Runnable() {
      def run() = f
    }

  def post[F](f: => F): Boolean = super.post(toRunnable(f))
}
