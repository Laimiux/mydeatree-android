package com.limeblast.rest

import com.google.gson.Gson
import java.io.{InputStreamReader, InputStream}


trait JsonModule {
  private val gson: Gson = new Gson()


  def getMainObject[T](jsonString: String, classOfT: Class[T]): T = gson.fromJson(jsonString, classOfT)

  def getMainObject[T](input: InputStream, classOfT: Class[T]): T = {
    val reader = new InputStreamReader(input)
    val mainObj = gson.fromJson(reader, classOfT)

    reader.close()
    input.close()

    return mainObj
  }

  def convertObjectToJson[T](obj: T): String = gson.toJson(obj)

  sealed trait ToJson {
    def obj: AnyRef

    def toJson(): String = convertObjectToJson(obj)

  }


  implicit def anyRefToJson(anyref: AnyRef): ToJson = new ToJson { val obj = anyref }

}
