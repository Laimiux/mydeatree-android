package com.limeblast.androidhelpers

import android.app.Activity
import android.content.{Context, Intent}


trait IntentConversions {
 implicit def activityToIntent[T <: Activity](activityClass: Class[T])(implicit context: Context): Intent = new Intent(context,activityClass)

}
