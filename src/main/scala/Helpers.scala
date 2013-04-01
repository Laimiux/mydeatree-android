package com.limeblast.mydeatree

import java.util.{GregorianCalendar, Date}
import java.text.SimpleDateFormat
import AppSettings._
import android.text.format.Time


/**
 * Should be renamed to PythonDateHelper
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

  def getNow(): String = {
    //Create a date
    val now = new Time()
    now.setToNow()

    now.format(PYTHON_DATE_FORMAT)
  }

}




