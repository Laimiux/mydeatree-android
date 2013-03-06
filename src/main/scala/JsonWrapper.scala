package com.limeblast.mydeatree

import java.io.{InputStreamReader, InputStream}
import com.google.gson.Gson

object JsonWrapper {
  val APP_TAG = "JsonWrapper"
  lazy val gson = new Gson()

  private def getGson(): Gson = gson

  def getMainObject[T](jsonString: String, classOfT: Class[T]): T = getGson().fromJson(jsonString, classOfT)


  def getMainObject[T](input: InputStream, classOfT: Class[T]): T = {
    val reader = new InputStreamReader(input)
    val mainObj = getGson().fromJson(reader, classOfT)

    reader.close()
    input.close()

    return mainObj
  }

  def convertObjectToJson[T](obj: T): String = getGson.toJson(obj)

}

