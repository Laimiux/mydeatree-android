package com.limeblast.androidhelpers

import android.app.Service


trait ScalifiedService extends Service {self: Service =>
  val context = self



}
