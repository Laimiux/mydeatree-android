package com.limeblast.mydeatree

import java.io.{InputStreamReader, InputStream}
import com.google.gson.Gson

object JsonWrapper {
  val APP_TAG = "JsonWrapper"
  var gson: Gson = _

  private def getGson(): Gson = {
    if (gson == null) {
      gson = new Gson()
    }
    gson
  }


  def getMainObject[T](jsonString: String, classOfT: Class[T]): T = {
    val gson = getGson()
    val obj = gson.fromJson(jsonString, classOfT)

    obj
  }

  def getMainObject[T](input: InputStream, classOfT: Class[T]): T = {
    val reader = new InputStreamReader(input)
    val gson = getGson()
    val mainObj = gson.fromJson(reader, classOfT)

    reader.close()
    input.close()

    return mainObj
  }

  def convertObjectToJson[T](obj: T): String = {
    val gson = getGson()
    gson.toJson(obj)
  }
}

