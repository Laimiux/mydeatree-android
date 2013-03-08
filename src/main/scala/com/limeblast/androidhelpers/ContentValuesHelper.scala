package com.limeblast.androidhelpers

import android.content.ContentValues
import android.content

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 3/7/13
 * Time: 4:25 PM
 * To change this template use File | Settings | File Templates.
 */
object ContentValuesHelper {
  def mapToContentValues(value_map: Map[String, Any]): ContentValues = {
     val values = new ContentValues()

    value_map.foreach(tuple => {
       val key = tuple._1

       tuple._2 match {
         case value: Int => values.put(key, value.asInstanceOf[java.lang.Integer])
         case value: String => values.put(key, value)
         case value: Long => values.put(key, value.asInstanceOf[java.lang.Long])
         case value: Boolean => values.put(key, value)
         case value: Array[Byte] => values.put(key, value)
         case value: Float => values.put(key, value.asInstanceOf[java.lang.Float])
         case value: Byte => values.put(key, value.asInstanceOf[java.lang.Byte])
         case value: Double => values.put(key, value)
         case value: Short => values.put(key, value.asInstanceOf[java.lang.Short])
         case _ =>
       }
    })

    values
  }

  implicit def mapToValues(map: Map[String, Any]): ContentValues = ContentValuesHelper.mapToContentValues(map)

}
