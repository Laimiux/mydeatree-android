package com.limeblast.mydeatree

import java.util.{GregorianCalendar, Comparator, Date}
import java.text.SimpleDateFormat
import java.text
import annotation.unchecked.uncheckedVariance
import android.content.Context
import android.app.{Activity, ActivityManager}
import android.util.Log
import scala.collection.JavaConversions._
import AppSettings._
import android.text.format.Time

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 1/7/13
 * Time: 10:02 PM
 * To change this template use File | Settings | File Templates.
 */
object Helpers {
  var dateFormat: SimpleDateFormat = _
  var calendar: GregorianCalendar = _

  def getDateFormat(): SimpleDateFormat = {
    if (dateFormat == null) {
      dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    }

    dateFormat
  }


  def stringToDate(str: String): Date = {
    // Cut microseconds from python date
    val newStr = str.substring(0, 18)

    val df = getDateFormat()
    df.parse(newStr)
  }

  def getCalendar(): GregorianCalendar = {
    if (calendar == null)
      calendar = new GregorianCalendar()


    calendar
  }


  def formatDate(date: Date): String = {
    new SimpleDateFormat("EEE, MMMM d").format(date)
  }

  def isServiceRunning(name: String, activity: Activity): Boolean = {
    activity.getSystemService(Context.ACTIVITY_SERVICE) match {
      case manager: ActivityManager => {
        for(service <- manager.getRunningServices(Integer.MAX_VALUE)) {
          if(name.equals(service.service.getClassName))
            return true
        }
      }
      case _ => if(AppSettings.DEBUG) Log.d(APP_TAG, "Activity manager not found")
    }

    return false
  }

  def getNow(): String = {
    //Create a date
    val now = new Time()
    now.setToNow()

    now.format(PYTHON_DATE_FORMAT)
  }

}




